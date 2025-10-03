package org.moneymanagement.Service;

import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Payload.Request.IncomeRequest;
import org.moneymanagement.Payload.Response.IncomeResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IncomeService {

    IncomeResponse addIncome(IncomeRequest incomeRequest);
    IncomeResponse updateExpense(ExpenseRequest incomeRequest);
    List<IncomeResponse> getCurrentMonthExpensesForCurrentUser();
    void deleteExpense(Long id);
    List<IncomeResponse> getLastest5ExpensesForCurrentUser();
    BigDecimal getTotalIncomesOfCurrentUser();
}
