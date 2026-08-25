package org.moneymanagement.service;

import org.moneymanagement.payload.request.ExpenseRequest;
import org.moneymanagement.payload.request.IncomeRequest;
import org.moneymanagement.payload.response.IncomeResponse;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {

    IncomeResponse addIncome(IncomeRequest incomeRequest);
    IncomeResponse updateExpense(ExpenseRequest incomeRequest);
    List<IncomeResponse> getCurrentMonthExpensesForCurrentUser();
    void deleteExpense(Long id);
    List<IncomeResponse> getLastest5ExpensesForCurrentUser();
    BigDecimal getTotalIncomesOfCurrentUser();
    List<IncomeResponse> filterIncome(LocalDate startDate, LocalDate endDate , String keyword , Sort sort);
}
