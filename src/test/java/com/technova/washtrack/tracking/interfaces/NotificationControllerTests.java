package com.technova.washtrack.tracking.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.ResultActions;

/** Each test uses its own recipient id because the in-memory store is shared by the context. */
@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions create(String recipientId, String stage) throws Exception {
        return mockMvc.perform(post("/api/v1/notifications").contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipientId\":" + recipientId + ",\"orderId\":5001,\"orderNumber\":\"ORD-005001\","
                        + "\"stage\":\"" + stage + "\"}"));
    }

    @Test
    void createsAndListsNewestFirst() throws Exception {
        create("9101", "RECEIVED").andExpect(status().isCreated()).andExpect(jsonPath("$.read").value(false));
        create("9101", "IN_PROCESS").andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/notifications").param("recipientId", "9101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].stage").value("IN_PROCESS"));
        mockMvc.perform(get("/api/v1/notifications").param("recipientId", "9199"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejectsInvalidRequests() throws Exception {
        create("9102", "TELEPORTED").andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/notifications").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.recipientId").exists())
                .andExpect(jsonPath("$.errors.stage").exists());
        mockMvc.perform(get("/api/v1/notifications")).andExpect(status().isBadRequest());
    }

    @Test
    void marksOneOrAllAsRead() throws Exception {
        String location = create("9103", "RECEIVED").andReturn().getResponse().getHeader("Location");
        create("9103", "IN_PROCESS");

        mockMvc.perform(patch(location + "/read")).andExpect(status().isOk()).andExpect(jsonPath("$.read").value(true));
        mockMvc.perform(get("/api/v1/notifications").param("recipientId", "9103").param("unread", "true"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/api/v1/notifications/read-all").param("recipientId", "9103"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/notifications").param("recipientId", "9103").param("unread", "true"))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(patch("/api/v1/notifications/999999/read")).andExpect(status().isNotFound());
    }
}
