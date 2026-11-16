package com.technova.washtrack.billing.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PlanControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsFreeBasicAndAdvancedPlans() throws Exception {
        mockMvc.perform(get("/api/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("FREE"))
                .andExpect(jsonPath("$[1].price").value(49))
                .andExpect(jsonPath("$[2].price").value(99))
                .andExpect(jsonPath("$[2].currency").value("PEN"));
    }

    @Test
    void returnsAPlanById() throws Exception {
        mockMvc.perform(get("/api/v1/plans/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BASIC"))
                .andExpect(jsonPath("$.features[2]").value("digital-payments"));
    }

    @Test
    void unknownPlanReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/plans/99")).andExpect(status().isNotFound());
    }
}
