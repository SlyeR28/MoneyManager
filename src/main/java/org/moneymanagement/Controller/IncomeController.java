package org.moneymanagement.Controller;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Payload.Request.IncomeRequest;
import org.moneymanagement.Payload.Response.IncomeResponse;
import org.moneymanagement.Service.IncomeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping("/add")
    public ResponseEntity<IncomeResponse> createExpense(@RequestBody IncomeRequest incomeRequest) {
        IncomeResponse incomeResponse = incomeService.addIncome(incomeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeResponse);
    }

    @GetMapping("/get")
    public ResponseEntity<List<IncomeResponse>> getAllIncomes() {
        List<IncomeResponse> forCurrentUser = incomeService.getCurrentMonthExpensesForCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(forCurrentUser);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?>deleteExpense(@PathVariable("expenseId") Long expenseId) {
        incomeService.deleteExpense(expenseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/top5")
    public ResponseEntity<List<IncomeResponse>> getTop5Incomes() {
        List<IncomeResponse> forCurrentUser = incomeService.getLastest5ExpensesForCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(forCurrentUser);
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalIncome() {
        BigDecimal currentUser = incomeService.getTotalIncomesOfCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(currentUser);
    }


}
