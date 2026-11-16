package com.technova.washtrack.billing.interfaces;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentActionsTests {

    @Autowired
    private MockMvc mockMvc;

    /** Registers a cash payment (stays PENDING) and returns its resource path. */
    private String pendingPayment() throws Exception {
        return mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":401,\"customerName\":\"Ana\",\"amount\":25,\"paymentMethod\":\"CASH\"}"))
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    void confirmsAPendingPayment() throws Exception {
        mockMvc.perform(post(pendingPayment() + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void rejectsAPendingPaymentWithReason() throws Exception {
        mockMvc.perform(post(pendingPayment() + "/reject").contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Counterfeit bill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Counterfeit bill"));
    }

    @Test
    void rejectRequiresAReason() throws Exception {
        mockMvc.perform(post(pendingPayment() + "/reject").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.reason").exists());
    }

    @Test
    void cannotActOnAnAlreadyResolvedPayment() throws Exception {
        String payment = pendingPayment();
        mockMvc.perform(post(payment + "/confirm")).andExpect(status().isOk());

        mockMvc.perform(post(payment + "/confirm")).andExpect(status().isUnprocessableContent());
        mockMvc.perform(post(payment + "/reject").contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"late\"}")).andExpect(status().isUnprocessableContent());
    }

    @Test
    void unknownPaymentReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/payments/999999/confirm")).andExpect(status().isNotFound());
    }
}
