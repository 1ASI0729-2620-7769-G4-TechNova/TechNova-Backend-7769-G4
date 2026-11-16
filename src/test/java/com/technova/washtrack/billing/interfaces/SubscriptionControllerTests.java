package com.technova.washtrack.billing.interfaces;

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

/** Each test uses its own laundry id because the in-memory store is shared across the test context. */
@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions subscribe(String laundryId, String planId) throws Exception {
        return mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"laundryId\":" + laundryId + ",\"planId\":" + planId + "}"));
    }

    @Test
    void subscribesALaundry() throws Exception {
        subscribe("601", "2")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/subscriptions/\\d+")))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.planId").value(2))
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists());
    }

    @Test
    void secondActiveSubscriptionIsRefused() throws Exception {
        subscribe("602", "1").andExpect(status().isCreated());

        subscribe("602", "3").andExpect(status().isUnprocessableContent());
    }

    @Test
    void rejectsInvalidRequests() throws Exception {
        subscribe("603", "99").andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/subscriptions").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.laundryId").exists())
                .andExpect(jsonPath("$.errors.planId").exists());
    }

    @Test
    void changesPlanOfAnActiveSubscription() throws Exception {
        String location = subscribe("606", "2").andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/change-plan").contentType(MediaType.APPLICATION_JSON)
                .content("{\"planId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value(3))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(post(location + "/change-plan").contentType(MediaType.APPLICATION_JSON)
                .content("{\"planId\":3}")).andExpect(status().isUnprocessableContent());
        mockMvc.perform(post(location + "/change-plan").contentType(MediaType.APPLICATION_JSON)
                .content("{\"planId\":99}")).andExpect(status().isNotFound());
        mockMvc.perform(post(location + "/change-plan").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getsASubscriptionByIdAndByLaundry() throws Exception {
        String location = subscribe("604", "2").andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.laundryId").value(604));
        mockMvc.perform(get("/api/v1/subscriptions").param("laundryId", "604"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/subscriptions").param("laundryId", "605"))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/v1/subscriptions/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/subscriptions")).andExpect(status().isBadRequest());
    }
}
