package com.technova.washtrack.delivery.interfaces;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Uses dates in 2099 (always in the future) and unique order ids: the in-memory store is shared by the context. */
@SpringBootTest
@AutoConfigureMockMvc
class DeliveryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions schedule(String orderId, String type, String when) throws Exception {
        return mockMvc.perform(post("/api/v1/deliveries").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":" + orderId + ",\"customerName\":\"Ana Martinez\",\"type\":\"" + type
                        + "\",\"address\":\"Av. Larco 123, Miraflores\",\"scheduledAt\":\"" + when + "\"}"));
    }

    @Test
    void schedulesADelivery() throws Exception {
        schedule("901", "DELIVERY", "2099-01-05T15:00:00Z")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/deliveries/\\d+")))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.type").value("DELIVERY"))
                .andExpect(jsonPath("$.orderId").value(901));
    }

    @Test
    void rejectsPastDatesAndInvalidRequests() throws Exception {
        schedule("902", "PICKUP", "2020-01-01T10:00:00Z").andExpect(status().isUnprocessableContent());
        schedule("902", "TELEPORT", "2099-01-06T10:00:00Z").andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/deliveries").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.orderId").exists())
                .andExpect(jsonPath("$.errors.address").exists())
                .andExpect(jsonPath("$.errors.scheduledAt").exists());
    }

    @Test
    void fullTimeSlotReturns409() throws Exception {
        schedule("911", "PICKUP", "2099-03-01T10:00:00Z").andExpect(status().isCreated());
        schedule("912", "PICKUP", "2099-03-01T10:15:00Z").andExpect(status().isCreated());
        schedule("913", "PICKUP", "2099-03-01T10:45:00Z").andExpect(status().isCreated());

        schedule("914", "PICKUP", "2099-03-01T10:30:00Z").andExpect(status().isConflict());
    }

    @Test
    void getsByIdAndFiltersTheList() throws Exception {
        String location = schedule("921", "PICKUP", "2099-04-01T09:00:00Z").andReturn().getResponse().getHeader("Location");
        schedule("921", "DELIVERY", "2099-04-03T09:00:00Z").andExpect(status().isCreated());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.orderId").value(921));
        mockMvc.perform(get("/api/v1/deliveries/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/deliveries").param("orderId", "921"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("PICKUP"));
        mockMvc.perform(get("/api/v1/deliveries").param("orderId", "921").param("type", "DELIVERY"))
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/deliveries").param("orderId", "921").param("from", "2099-04-02").param("to", "2099-04-30"))
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/deliveries").param("orderId", "921").param("status", "COMPLETED"))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
