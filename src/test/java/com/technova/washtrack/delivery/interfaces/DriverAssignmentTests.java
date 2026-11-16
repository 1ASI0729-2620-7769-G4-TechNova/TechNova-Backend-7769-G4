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
class DriverAssignmentTests {

    @Autowired
    private MockMvc mockMvc;

    private String driver(String name) throws Exception {
        String location = mockMvc.perform(post("/api/v1/drivers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}")).andReturn().getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private String delivery(String orderId, String when) throws Exception {
        return mockMvc.perform(post("/api/v1/deliveries").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":" + orderId + ",\"customerName\":\"Ana\",\"type\":\"PICKUP\","
                        + "\"address\":\"Jr. Union 456, Lima\",\"scheduledAt\":\"" + when + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
    }

    private org.springframework.test.web.servlet.ResultActions assign(String deliveryLocation, String driverId)
            throws Exception {
        return mockMvc.perform(patch(deliveryLocation).contentType(MediaType.APPLICATION_JSON)
                .content("{\"driverId\":" + driverId + "}"));
    }

    @Test
    void assignsAnAvailableDriver() throws Exception {
        String driverId = driver("Luis Quispe");

        assign(delivery("1001", "2099-05-01T10:00:00Z"), driverId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverName").value("Luis Quispe"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void unavailableOrUnknownDriversAreRefused() throws Exception {
        String driverId = driver("Off Duty");
        mockMvc.perform(patch("/api/v1/drivers/" + driverId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"available\":false}"));
        String location = delivery("1002", "2099-05-02T10:00:00Z");

        assign(location, driverId).andExpect(status().isUnprocessableContent());
        assign(location, "999999").andExpect(status().isNotFound());
        assign(location, "0").andExpect(status().isBadRequest());
        assign("/api/v1/deliveries/999999", driverId).andExpect(status().isNotFound());
    }

    @Test
    void aDriverCannotTakeTwoDeliveriesInTheSameHour() throws Exception {
        String driverId = driver("Busy Driver");
        assign(delivery("1003", "2099-05-03T10:00:00Z"), driverId).andExpect(status().isOk());
        String second = delivery("1004", "2099-05-03T10:30:00Z");
        String later = delivery("1005", "2099-05-03T11:00:00Z");

        assign(second, driverId).andExpect(status().isConflict());
        assign(later, driverId).andExpect(status().isOk());
    }
}
