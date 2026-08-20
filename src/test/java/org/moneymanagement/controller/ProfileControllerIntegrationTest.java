package org.moneymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.Payload.Request.AuthRequest;
import org.moneymanagement.Payload.Request.ProfileRequest;
import org.moneymanagement.Service.impl.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    @Test
    @DisplayName("POST /api/v1/register should successfully create profile with 201 Created")
    void testRegisterUser_Success() throws Exception {
        ProfileRequest request = ProfileRequest.builder()
                .fullName("New Registered User")
                .email("newuser@example.com")
                .password("SecurePass@123")
                .build();

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("New Registered User"));
    }

    @Test
    @DisplayName("POST /api/v1/register should return 400 Bad Request when validation fails")
    void testRegisterUser_ValidationFailure() throws Exception {
        ProfileRequest request = ProfileRequest.builder()
                .fullName("No")
                .email("invalid-email")
                .password("weak")
                .build();

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/activation should activate account when valid token is provided")
    void testActivateProfile_Success() throws Exception {
        mockMvc.perform(get("/api/v1/activation")
                        .param("token", "sample-activation-token-12345"))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile activated successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/activation should return 404 Not Found when token does not exist")
    void testActivateProfile_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/activation")
                        .param("token", "non-existent-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/login should return 403 Forbidden for inactive account")
    void testLogin_InactiveAccount() throws Exception {
        AuthRequest authRequest = AuthRequest.builder()
                .email("inactive@example.com")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account not activated. Please check your email."));
    }

    @Test
    @DisplayName("POST /api/v1/login should return 401 Unauthorized for incorrect password")
    void testLogin_InvalidCredentials() throws Exception {
        AuthRequest authRequest = AuthRequest.builder()
                .email("testuser@example.com")
                .password("WrongPassword@999")
                .build();

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }
}
