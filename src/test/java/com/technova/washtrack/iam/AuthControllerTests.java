package com.technova.washtrack.iam;

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
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private org.springframework.test.web.servlet.ResultActions login(String body) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void acceptsTheDemoCredentials() throws Exception {
        login("{\"username\":\"demo\",\"password\":\"demo123\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("demo-token"));
    }

    @Test
    void rejectsWrongCredentials() throws Exception {
        login("{\"username\":\"demo\",\"password\":\"nope\"}").andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsBlankFields() throws Exception {
        login("{\"username\":\"\",\"password\":\"\"}").andExpect(status().isBadRequest());
    }
}
