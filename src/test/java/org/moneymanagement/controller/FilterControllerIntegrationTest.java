package org.moneymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.Payload.Request.FilterDto;
import org.moneymanagement.Security.Jwt.JwtUtils;
import org.moneymanagement.Service.impl.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FilterControllerIntegrationTest {

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
    @DisplayName("POST /api/filter/ with type 'income' should filter income transactions")
    void testFilterIncomeTransactions() throws Exception {
        FilterDto filterDto = FilterDto.builder()
                .type("income")
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .keyword("Salary")
                .sortField("date")
                .sortOrder("desc")
                .build();

        mockMvc.perform(post("/api/filter/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/filter/ with type 'expense' should filter expense transactions")
    void testFilterExpenseTransactions() throws Exception {
        FilterDto filterDto = FilterDto.builder()
                .type("expense")
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .keyword("Shopping")
                .sortField("date")
                .sortOrder("asc")
                .build();

        mockMvc.perform(post("/api/filter/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/filter/ with invalid type should return 400 Bad Request")
    void testFilterTransactions_InvalidType() throws Exception {
        FilterDto filterDto = FilterDto.builder()
                .type("unknown")
                .build();

        mockMvc.perform(post("/api/filter/")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid filter type"));
    }
}
