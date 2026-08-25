package org.moneymanagement.service;

import org.moneymanagement.payload.request.ExpenseRequest;
import org.moneymanagement.payload.response.ExpenseResponse;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    ExpenseResponse addExpense(ExpenseRequest expenseRequest);
    ExpenseResponse updateExpense(ExpenseRequest expenseRequest);
    List<ExpenseResponse> getCurrentMonthExpensesForCurrentUser();
    void deleteExpense(Long id);
    List<ExpenseResponse>getLastest5ExpensesForCurrentUser();
    BigDecimal getTotalExpensesOfCurrentUser();
    List<ExpenseResponse> filterExpenses(LocalDate startDate, LocalDate endDate , String keyword , Sort sort);
    List<ExpenseResponse>getExpensesByUserOnDate(Long profileId , LocalDate date);
}
