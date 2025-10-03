package org.moneymanagement.Controller;


import lombok.RequiredArgsConstructor;
import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Payload.Response.ExpenseResponse;
import org.moneymanagement.Service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/add")
    public ResponseEntity<ExpenseResponse> createExpense(@RequestBody ExpenseRequest expenseRequest) {
        ExpenseResponse expenseResponse = expenseService.addExpense(expenseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseResponse);
    }

    @GetMapping("/get")
    public ResponseEntity<List<ExpenseResponse>> getAllIncomes() {
        List<ExpenseResponse> forCurrentUser = expenseService.getCurrentMonthExpensesForCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(forCurrentUser);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteExpense(@PathVariable("expenseId") Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/top5")
    public ResponseEntity<List<ExpenseResponse>> getTop5Expenses() throws  Exception {
        List<ExpenseResponse> forCurrentUser = expenseService.getLastest5ExpensesForCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(forCurrentUser);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalExpenses() throws  Exception {
        BigDecimal currentUser = expenseService.getTotalExpensesOfCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(currentUser);
    }


}
