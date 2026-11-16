package com.technova.washtrack.shared.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTests.ProbeController.class)
class GlobalExceptionHandlerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void notFoundReturns404() throws Exception {
        mockMvc.perform(get("/probe/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Probe with id 7 was not found"));
    }

    @Test
    void businessRuleReturns422() throws Exception {
        mockMvc.perform(get("/probe/rule"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("rule broken"));
    }

    @Test
    void invalidBodyReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/probe/body").contentType(MediaType.APPLICATION_JSON).content("{\"amount\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @RestController
    static class ProbeController {

        record Body(@Positive int amount) {
        }

        @GetMapping("/probe/missing")
        void missing() {
            throw new ResourceNotFoundException("Probe", 7);
        }

        @GetMapping("/probe/rule")
        void rule() {
            throw new BusinessRuleException("rule broken");
        }

        @PostMapping("/probe/body")
        void body(@Valid @RequestBody Body body) {
        }
    }
}
