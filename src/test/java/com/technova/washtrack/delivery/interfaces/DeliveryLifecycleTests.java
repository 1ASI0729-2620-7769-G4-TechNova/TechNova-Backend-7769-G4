package com.technova.washtrack.delivery.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Uses dates in 2099 and unique order ids: the in-memory store is shared by the test context. */
@SpringBootTest
@AutoConfigureMockMvc
class DeliveryLifecycleTests {

    @Autowired
    private MockMvc mockMvc;

    private String scheduled(String orderId, String when) throws Exception {
        return mockMvc.perform(post("/api/v1/deliveries").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":" + orderId + ",\"customerName\":\"Ana\",\"type\":\"DELIVERY\","
                        + "\"address\":\"Av. Larco 123\",\"scheduledAt\":\"" + when + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
    }

    private void assignNewDriver(String delivery, String name) throws Exception {
        String driver = mockMvc.perform(post("/api/v1/drivers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}")).andReturn().getResponse().getHeader("Location");
        mockMvc.perform(patch(delivery).contentType(MediaType.APPLICATION_JSON)
                .content("{\"driverId\":" + driver.substring(driver.lastIndexOf('/') + 1) + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void walksFromScheduledToCompleted() throws Exception {
        String delivery = scheduled("1101", "2099-06-01T10:00:00Z");
        assignNewDriver(delivery, "Lifecycle Driver 1");

        mockMvc.perform(post(delivery + "/start")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_THE_WAY"));
        mockMvc.perform(post(delivery + "/complete")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
        mockMvc.perform(post(delivery + "/complete")).andExpect(status().isUnprocessableContent());
        mockMvc.perform(post(delivery + "/fail").contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"late\"}")).andExpect(status().isUnprocessableContent());
    }

    @Test
    void cannotStartWithoutADriverOrCompleteWithoutStarting() throws Exception {
        String delivery = scheduled("1102", "2099-06-02T10:00:00Z");

        mockMvc.perform(post(delivery + "/start")).andExpect(status().isUnprocessableContent());
        mockMvc.perform(post(delivery + "/complete")).andExpect(status().isUnprocessableContent());
        mockMvc.perform(post("/api/v1/deliveries/999999/start")).andExpect(status().isNotFound());
    }

    @Test
    void failingKeepsTheReasonAndFreesTheSlot() throws Exception {
        String delivery = scheduled("1103", "2099-06-03T10:00:00Z");

        mockMvc.perform(post(delivery + "/fail").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(delivery + "/fail").contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Nobody at the address\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureReason").value("Nobody at the address"));
        scheduled("1103", "2099-06-04T10:00:00Z");
    }
}
