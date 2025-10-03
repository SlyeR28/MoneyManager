package org.moneymanagement.Service;

import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Payload.Response.ExpenseResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {

    ExpenseResponse addExpense(ExpenseRequest expenseRequest);
    ExpenseResponse updateExpense(ExpenseRequest expenseRequest);
    List<ExpenseResponse> getCurrentMonthExpensesForCurrentUser();
    void deleteExpense(Long id);
    List<ExpenseResponse>getLastest5ExpensesForCurrentUser();
    BigDecimal getTotalExpensesOfCurrentUser();
}
