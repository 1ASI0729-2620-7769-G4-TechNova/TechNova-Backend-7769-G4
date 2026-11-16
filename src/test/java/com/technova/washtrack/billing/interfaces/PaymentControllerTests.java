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

/** Each test uses its own order id because the in-memory store is shared across the test context. */
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions register(String orderId, String amount) throws Exception {
        String amountField = amount == null ? "" : ",\"amount\":" + amount;
        return mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":" + orderId + ",\"customerName\":\"Ana Martinez\","
                        + "\"paymentMethod\":\"CASH\"" + amountField + "}"));
    }

    @Test
    void registersACashPaymentAsPending() throws Exception {
        register("201", "45.5")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/payments/\\d+")))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.orderId").value(201))
                .andExpect(jsonPath("$.reference", matchesPattern("PAY-\\d{4}")))
                .andExpect(jsonPath("$.paidAt").exists());
    }

    @Test
    void digitalPaymentIsVerifiedAutomatically() throws Exception {
        mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":207,\"customerName\":\"Ana\",\"amount\":10,\"paymentMethod\":\"YAPE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void rejectsNegativeMissingAndUnknownMethodRequests() throws Exception {
        register("202", "-5").andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.amount").exists());
        register("202", null).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.amount").exists());
        mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":202,\"customerName\":\"Ana\",\"amount\":10,\"paymentMethod\":\"BITCOIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getsAPaymentById() throws Exception {
        String location = register("203", "30").andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.orderId").value(203));
        mockMvc.perform(get("/api/v1/payments/999999")).andExpect(status().isNotFound());
    }

    @Test
    void historyFiltersByOrderAndShowsEmptyResult() throws Exception {
        register("204", "10");
        register("204", "20");
        register("205", "99");

        mockMvc.perform(get("/api/v1/payments").param("orderId", "204"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/v1/payments").param("orderId", "204").param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historyFiltersByDateRange() throws Exception {
        register("206", "15");

        mockMvc.perform(get("/api/v1/payments").param("orderId", "206").param("from", "2000-01-01").param("to", "2000-12-31"))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/v1/payments").param("orderId", "206").param("from", "2000-01-01").param("to", "2999-12-31"))
                .andExpect(jsonPath("$.length()").value(1));
    }
}
