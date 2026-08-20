package org.moneymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.Payload.Request.IncomeRequest;
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
class IncomeControllerIntegrationTest {

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
    @DisplayName("POST /api/incomes/add should add new income record")
    void testAddIncome() throws Exception {
        IncomeRequest request = IncomeRequest.builder()
                .name("Yearly Bonus")
                .amount(new BigDecimal("2000.00"))
                .icon("star")
                .date(LocalDate.now())
                .categoryId(1L)
                .build();

        mockMvc.perform(post("/api/incomes/add")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Yearly Bonus"))
                .andExpect(jsonPath("$.amount").value(2000.00));
    }

    @Test
    @DisplayName("GET /api/incomes/get should return current month incomes")
    void testGetCurrentMonthIncomes() throws Exception {
        mockMvc.perform(get("/api/incomes/get")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/incomes/top5 should return latest 5 incomes")
    void testGetTop5Incomes() throws Exception {
        mockMvc.perform(get("/api/incomes/top5")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/incomes/total should return total income amount")
    void testGetTotalIncome() throws Exception {
        mockMvc.perform(get("/api/incomes/total")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    @DisplayName("DELETE /api/incomes/{id} should delete income record")
    void testDeleteIncome() throws Exception {
        mockMvc.perform(delete("/api/incomes/2")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }
}
