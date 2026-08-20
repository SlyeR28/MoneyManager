package org.moneymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Security.Jwt.JwtUtils;
import org.moneymanagement.Service.impl.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerIntegrationTest {

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
    @DisplayName("POST /api/expenses/add should add new expense record")
    void testAddExpense() throws Exception {
        ExpenseRequest request = ExpenseRequest.builder()
                .name("Gym Membership")
                .amount(new BigDecimal("50.00"))
                .icon("activity")
                .date(LocalDate.now())
                .categoryId(3L)
                .build();

        mockMvc.perform(post("/api/expenses/add")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Gym Membership"))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    @DisplayName("GET /api/expenses/get should return current month expenses")
    void testGetCurrentMonthExpenses() throws Exception {
        mockMvc.perform(get("/api/expenses/get")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/expenses/top5 should return latest 5 expenses")
    void testGetTop5Expenses() throws Exception {
        mockMvc.perform(get("/api/expenses/top5")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/expenses/total should return total expense amount")
    void testGetTotalExpenses() throws Exception {
        mockMvc.perform(get("/api/expenses/total")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    @DisplayName("DELETE /api/expenses/{id} should delete expense record")
    void testDeleteExpense() throws Exception {
        mockMvc.perform(delete("/api/expenses/2")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }
}
