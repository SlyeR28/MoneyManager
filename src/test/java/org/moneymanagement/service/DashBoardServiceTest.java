package org.moneymanagement.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.payload.request.RecentTransactionDto;
import org.moneymanagement.payload.response.ExpenseResponse;
import org.moneymanagement.payload.response.IncomeResponse;
import org.moneymanagement.service.ExpenseService;
import org.moneymanagement.service.IncomeService;
import org.moneymanagement.service.ProfileService;
import org.moneymanagement.service.impl.DashBoardService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashBoardServiceTest {

    @Mock
    private IncomeService incomeService;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private DashBoardService dashBoardService;

    @Test
    @DisplayName("getDashBoardData should calculate balance and return merged recent transactions")
    void testGetDashBoardData() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();

        IncomeResponse income = IncomeResponse.builder()
                .id(1L)
                .name("Salary")
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now())
                .creationAt(LocalDateTime.now().minusHours(2))
                .build();

        ExpenseResponse expense = ExpenseResponse.builder()
                .id(2L)
                .name("Groceries")
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.now())
                .creationAt(LocalDateTime.now().minusHours(1))
                .build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeService.getLastest5ExpensesForCurrentUser()).thenReturn(List.of(income));
        when(expenseService.getLastest5ExpensesForCurrentUser()).thenReturn(List.of(expense));
        when(incomeService.getTotalIncomesOfCurrentUser()).thenReturn(new BigDecimal("5000.00"));
        when(expenseService.getTotalExpensesOfCurrentUser()).thenReturn(new BigDecimal("200.00"));

        Map<String, Object> data = dashBoardService.getDashBoardData();

        assertThat(data).isNotNull();
        assertThat(data.get("Total Income")).isEqualTo(new BigDecimal("5000.00"));
        assertThat(data.get("Total Expense")).isEqualTo(new BigDecimal("200.00"));
        assertThat(data.get("Total Balance")).isEqualTo(new BigDecimal("4800.00"));
        assertThat(data.get("recent5Incomes")).isEqualTo(List.of(income));
        assertThat(data.get("recent5Expenses")).isEqualTo(List.of(expense));

        @SuppressWarnings("unchecked")
        List<RecentTransactionDto> transactions = (List<RecentTransactionDto>) data.get("recentTransaction");
        assertThat(transactions).hasSize(2);
    }
}
