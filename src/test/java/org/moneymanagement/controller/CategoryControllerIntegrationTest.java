package org.moneymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.payload.request.CategoryRequest;
import org.moneymanagement.security.jwt.JwtUtils;
import org.moneymanagement.service.impl.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private EmailService emailService;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        jwtToken = jwtUtils.generateToken("testuser@example.com");
    }

    @Test
    @DisplayName("GET /api/category/ should reject unauthenticated requests")
    void testGetCategories_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/category/"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/category/ should return all categories for authenticated user")
    void testGetCategories_Authenticated() throws Exception {
        mockMvc.perform(get("/api/category/")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(4)));
    }

    @Test
    @DisplayName("GET /api/category/{type} should return categories filtered by type")
    void testGetCategoriesByType() throws Exception {
        mockMvc.perform(get("/api/category/income")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/category/create should create new category")
    void testCreateCategory() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Investments")
                .type("income")
                .icon("trending-up")
                .profileId(1L)
                .build();

        mockMvc.perform(post("/api/category/create")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.name").value("Investments"));
    }

    @Test
    @DisplayName("PUT /api/category/{categoryId} should update existing category")
    void testUpdateCategory() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Updated Salary")
                .type("income")
                .icon("money-bill")
                .build();

        mockMvc.perform(put("/api/category/1")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Salary"));
    }
}
