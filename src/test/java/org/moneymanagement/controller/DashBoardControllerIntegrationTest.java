package org.moneymanagement.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.security.jwt.JwtUtils;
import org.moneymanagement.service.impl.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashBoardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("GET /dashboard/ should return financial summary for authenticated user")
    void testGetDashBoardData() throws Exception {
        mockMvc.perform(get("/dashboard/")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['Total Balance']").exists())
                .andExpect(jsonPath("$['Total Income']").exists())
                .andExpect(jsonPath("$['Total Expense']").exists())
                .andExpect(jsonPath("$['recent5Expenses']").isArray())
                .andExpect(jsonPath("$['recent5Incomes']").isArray())
                .andExpect(jsonPath("$['recentTransaction']").isArray());
    }

    @Test
    @DisplayName("GET /dashboard/ should reject unauthenticated request")
    void testGetDashBoardData_Unauthenticated() throws Exception {
        mockMvc.perform(get("/dashboard/"))
                .andExpect(status().isForbidden());
    }
}
