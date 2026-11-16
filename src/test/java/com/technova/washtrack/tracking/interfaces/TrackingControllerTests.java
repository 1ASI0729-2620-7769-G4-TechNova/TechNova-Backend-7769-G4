package com.technova.washtrack.tracking.interfaces;

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
class TrackingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions start(String orderId, String customerId) throws Exception {
        return mockMvc.perform(post("/api/v1/orderTrackings").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":" + orderId + ",\"orderNumber\":\"ORD-" + orderId + "\",\"customerId\":"
                        + customerId + "}"));
    }

    @Test
    void startsTrackingInTheReceivedStage() throws Exception {
        start("3001", "8001")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/orderTrackings/\\d+")))
                .andExpect(jsonPath("$.stage").value("RECEIVED"))
                .andExpect(jsonPath("$.orderNumber").value("ORD-3001"));
    }

    @Test
    void rejectsInvalidAndDuplicateRequests() throws Exception {
        mockMvc.perform(post("/api/v1/orderTrackings").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.orderId").exists())
                .andExpect(jsonPath("$.errors.orderNumber").exists());

        start("3002", "8002").andExpect(status().isCreated());
        start("3002", "8002").andExpect(status().isUnprocessableContent());
    }

    @Test
    void advancesStageByStageAndKeepsTheHistory() throws Exception {
        String location = start("3003", "8003").andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/advance")).andExpect(jsonPath("$.stage").value("IN_PROCESS"));
        mockMvc.perform(post(location + "/advance")).andExpect(jsonPath("$.stage").value("READY_FOR_DELIVERY"));
        mockMvc.perform(post(location + "/advance")).andExpect(jsonPath("$.stage").value("DELIVERED"));
        mockMvc.perform(post(location + "/advance")).andExpect(status().isUnprocessableContent());

        mockMvc.perform(get("/api/v1/stageChanges").param("orderId", "3003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].stage").value("RECEIVED"))
                .andExpect(jsonPath("$[3].stage").value("DELIVERED"));
        mockMvc.perform(get("/api/v1/stageChanges")).andExpect(status().isBadRequest());
    }

    @Test
    void getsAndListsTrackings() throws Exception {
        String location = start("3004", "8004").andReturn().getResponse().getHeader("Location");
        start("3005", "8004").andExpect(status().isCreated());

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.customerId").value(8004));
        mockMvc.perform(get("/api/v1/orderTrackings/999999")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/orderTrackings/999999/advance")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/orderTrackings").param("customerId", "8004"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(3005));
    }
}
