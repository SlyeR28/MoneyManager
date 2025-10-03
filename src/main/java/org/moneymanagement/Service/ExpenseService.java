package org.moneymanagement.Service;

import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Payload.Response.ExpenseResponse;
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
