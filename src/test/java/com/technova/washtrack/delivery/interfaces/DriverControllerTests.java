package com.technova.washtrack.delivery.interfaces;

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
class DriverControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private String register(String name) throws Exception {
        return mockMvc.perform(post("/api/v1/drivers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}")).andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    void registersADriverAsAvailable() throws Exception {
        String location = register("Luis Quispe");

        mockMvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis Quispe"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void rejectsABlankNameAndUnknownIds() throws Exception {
        mockMvc.perform(post("/api/v1/drivers").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists());
        mockMvc.perform(get("/api/v1/drivers/999999")).andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/v1/drivers/999999").contentType(MediaType.APPLICATION_JSON)
                .content("{\"available\":false}")).andExpect(status().isNotFound());
    }

    @Test
    void unavailableDriversAreHiddenFromTheAvailableList() throws Exception {
        String location = register("Zoe Unavailable");

        mockMvc.perform(patch(location).contentType(MediaType.APPLICATION_JSON).content("{\"available\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(get("/api/v1/drivers").param("availableOnly", "true"))
                .andExpect(jsonPath("$[?(@.name=='Zoe Unavailable')]").isEmpty());
        mockMvc.perform(get("/api/v1/drivers"))
                .andExpect(jsonPath("$[?(@.name=='Zoe Unavailable')]").isNotEmpty());
        mockMvc.perform(patch(location).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
