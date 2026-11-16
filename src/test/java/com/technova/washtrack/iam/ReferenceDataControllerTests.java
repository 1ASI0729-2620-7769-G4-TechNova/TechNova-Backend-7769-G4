package com.technova.washtrack.iam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

@SpringBootTest
@AutoConfigureMockMvc
class ReferenceDataControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void filtersUsersByEmailAndPassword() throws Exception {
        mockMvc.perform(get("/api/v1/users").param("email", "demo").param("password", "demo123")
                        .param("accountType", "LAUNDRY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(6));
        mockMvc.perform(get("/api/v1/users").param("email", "demo").param("password", "x"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void seedsRolesAndLaundryServices() throws Exception {
        mockMvc.perform(get("/api/v1/roles")).andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/v1/laundryServices")).andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void createsPatchesAndDeletesARow() throws Exception {
        String location = mockMvc.perform(post("/api/v1/laundryServices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Planchado\",\"price\":4,\"active\":true}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        mockMvc.perform(patch(location).contentType(MediaType.APPLICATION_JSON).content("{\"price\":5}"))
                .andExpect(jsonPath("$.price").value(5))
                .andExpect(jsonPath("$.name").value("Planchado"));
        mockMvc.perform(delete(location)).andExpect(status().isNoContent());
        mockMvc.perform(get(location)).andExpect(status().isNotFound());
    }
}
