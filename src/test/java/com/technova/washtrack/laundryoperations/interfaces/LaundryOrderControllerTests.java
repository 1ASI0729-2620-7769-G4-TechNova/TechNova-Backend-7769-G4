package com.technova.washtrack.laundryoperations.interfaces;

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

/** Each test uses its own order and customer ids because the in-memory store is shared by the context. */
@SpringBootTest
@AutoConfigureMockMvc
class LaundryOrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions receive(String orderId, String customerId, int garmentCount) throws Exception {
        return mockMvc.perform(post("/api/v1/laundryOrders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":" + orderId + ",\"orderNumber\":\"ORD-" + orderId + "\",\"customerId\":"
                        + customerId + ",\"garmentCount\":" + garmentCount + "}"));
    }

    @Test
    void receivesLaundryOrderInReceivedStatus() throws Exception {
        receive("7101", "8101", 4)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/laundryOrders/\\d+")))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.orderNumber").value("ORD-7101"))
                .andExpect(jsonPath("$.garmentCount").value(4));
    }

    @Test
    void rejectsInvalidAndDuplicateRequests() throws Exception {
        mockMvc.perform(post("/api/v1/laundryOrders").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.orderId").exists())
                .andExpect(jsonPath("$.errors.orderNumber").exists())
                .andExpect(jsonPath("$.errors.customerId").exists())
                .andExpect(jsonPath("$.errors.garmentCount").exists());

        receive("7102", "8102", 0).andExpect(status().isBadRequest());

        receive("7103", "8103", 2).andExpect(status().isCreated());
        receive("7103", "8103", 2).andExpect(status().isUnprocessableContent());
    }

    @Test
    void advancesThroughEveryStageUntilReadyForDelivery() throws Exception {
        String location = receive("7104", "8104", 3).andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/advance")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLASSIFIED"));
        mockMvc.perform(post(location + "/advance")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WASHING"));
        mockMvc.perform(post(location + "/advance")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRYING"));
        mockMvc.perform(post(location + "/advance")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IRONING"));
        mockMvc.perform(post(location + "/advance")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY_FOR_DELIVERY"));

        mockMvc.perform(post(location + "/advance")).andExpect(status().isUnprocessableContent());
    }

    @Test
    void getsAndFiltersLaundryOrders() throws Exception {
        String location = receive("7105", "8105", 2).andReturn().getResponse().getHeader("Location");
        String second = receive("7106", "8105", 6).andReturn().getResponse().getHeader("Location");
        mockMvc.perform(post(second + "/advance")).andExpect(status().isOk());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.customerId").value(8105));
        mockMvc.perform(get("/api/v1/laundryOrders/999999")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/laundryOrders/999999/advance")).andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/laundryOrders").param("customerId", "8105"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/v1/laundryOrders").param("customerId", "8105").param("status", "CLASSIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(7106));
    }
}
