package com.technova.washtrack.customer.interfaces;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.jayway.jsonpath.JsonPath;

/** Each test uses its own user id because the in-memory store is shared across the test context. */
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions register(String userId, String phone) throws Exception {
        String userField = userId == null ? "" : "\"userId\":" + userId + ",";
        return mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content("{" + userField + "\"fullName\":\"Ana Martinez\",\"phone\":\"" + phone + "\"}"));
    }

    private String registerAndGetLocation(String userId) throws Exception {
        return register(userId, "987654321").andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
    }

    private ResultActions addAddress(String location, String street, String district) throws Exception {
        return mockMvc.perform(post(location + "/addresses").contentType(MediaType.APPLICATION_JSON)
                .content("{\"street\":\"" + street + "\",\"district\":\"" + district + "\","
                        + "\"reference\":\"Frente al parque\",\"contactPhone\":\"987654321\"}"));
    }

    @Test
    void registersACustomerWithoutAddresses() throws Exception {
        register("6001", "987654321")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/customers/\\d+")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(6001))
                .andExpect(jsonPath("$.fullName").value("Ana Martinez"))
                .andExpect(jsonPath("$.addresses.length()").value(0));
    }

    @Test
    void rejectsInvalidRegistrationData() throws Exception {
        register("6002", "abc").andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.phone").exists());
        register(null, "987654321").andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.userId").exists());
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":6002,\"fullName\":\" \",\"phone\":\"987654321\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    void aUserCannotRegisterTwice() throws Exception {
        registerAndGetLocation("6003");

        register("6003", "912345678").andExpect(status().isConflict());
    }

    @Test
    void getsACustomerByIdAndFiltersByUser() throws Exception {
        String location = registerAndGetLocation("6004");

        mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(6004));
        mockMvc.perform(get("/api/v1/customers/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/customers").param("userId", "6004"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/customers").param("userId", "6999"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updatesTheProfile() throws Exception {
        String location = registerAndGetLocation("6005");

        mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Ana Perez\",\"phone\":\"912345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ana Perez"))
                .andExpect(jsonPath("$.phone").value("912345678"));
        mockMvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"\",\"phone\":\"912345678\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/customers/999999").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Ana\",\"phone\":\"912345678\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void firstAddressIsTheDefaultAndInvalidOnesAreRejected() throws Exception {
        String location = registerAndGetLocation("6006");

        addAddress(location, "Av. Arequipa 123", "Miraflores")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addresses.length()").value(1))
                .andExpect(jsonPath("$.addresses[0].defaultAddress").value(true))
                .andExpect(jsonPath("$.addresses[0].district").value("Miraflores"));

        mockMvc.perform(post(location + "/addresses").contentType(MediaType.APPLICATION_JSON)
                .content("{\"street\":\"\",\"district\":\"\",\"contactPhone\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.street").exists())
                .andExpect(jsonPath("$.errors.district").exists())
                .andExpect(jsonPath("$.errors.contactPhone").exists());
        addAddress(location, "Av. Arequipa 123", "Miraflores").andExpect(status().isUnprocessableContent());
        addAddress("/api/v1/customers/999999", "Calle 1", "Lince").andExpect(status().isNotFound());
    }

    @Test
    void changesTheDefaultAddressAndRemovesAddresses() throws Exception {
        String location = registerAndGetLocation("6007");
        addAddress(location, "Av. Arequipa 123", "Miraflores");
        addAddress(location, "Calle Los Pinos 45", "San Isidro");

        mockMvc.perform(get(location)).andExpect(jsonPath("$.addresses[1].defaultAddress").value(false));
        Integer secondId = JsonPath.read(
                mockMvc.perform(get(location)).andReturn().getResponse().getContentAsString(),
                "$.addresses[1].id");

        mockMvc.perform(post(location + "/addresses/" + secondId + "/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addresses[0].defaultAddress").value(false))
                .andExpect(jsonPath("$.addresses[1].defaultAddress").value(true));
        mockMvc.perform(post(location + "/addresses/999999/default")).andExpect(status().isNotFound());

        mockMvc.perform(delete(location + "/addresses/" + secondId)).andExpect(status().isNoContent());
        mockMvc.perform(get(location))
                .andExpect(jsonPath("$.addresses.length()").value(1))
                .andExpect(jsonPath("$.addresses[0].defaultAddress").value(true));
        mockMvc.perform(delete(location + "/addresses/" + secondId)).andExpect(status().isNotFound());
    }
}
