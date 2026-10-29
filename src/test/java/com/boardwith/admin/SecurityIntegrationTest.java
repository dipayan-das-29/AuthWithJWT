package com.boardwith.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicEndpointIsOpen() throws Exception {
        mockMvc.perform(get("/api/public/hello"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void loginReturnsJwtToken() throws Exception {
        mockMvc.perform(post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("admin", "admin123"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("admin", "wrong"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanAccessAdminEndpointWithJwt() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void userWithoutRolesCannotAccessAdminEndpoint() throws Exception {
        // Register a new user (no roles assigned), then try to access admin endpoint
        mockMvc.perform(post("/api/public/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.boardwith.admin.dto.request.RegistrationRequest("newuser", "password123"))))
            .andExpect(status().isCreated());

        String token = loginAndGetToken("newuser", "password123");

        mockMvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void missingTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationRejectsShortPassword() throws Exception {
        mockMvc.perform(post("/api/public/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.boardwith.admin.dto.request.RegistrationRequest("someone", "short"))))
            .andExpect(status().isBadRequest());
    }

    /* ---------- helpers ---------- */

    private String loginAndGetToken(String username, String password) throws Exception {
        var result = mockMvc.perform(post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
            .andExpect(status().isOk())
            .andReturn();
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    public record LoginRequest(String username, String password) {}
}
