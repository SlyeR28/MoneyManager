package org.moneymanagement.Controller;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Payload.Request.FilterDto;
import org.moneymanagement.Payload.Response.ExpenseResponse;
import org.moneymanagement.Payload.Response.IncomeResponse;
import org.moneymanagement.Service.ExpenseService;
import org.moneymanagement.Service.IncomeService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/filter")
public class FilterController {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @PostMapping("/")
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDto filterDto) {
        LocalDate startDate = filterDto.getStartDate() != null ? filterDto.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filterDto.getEndDate() != null ? filterDto.getEndDate() : LocalDate.now();
        String keyword = filterDto.getKeyword() != null ? filterDto.getKeyword() : "";
        String sortField = filterDto.getSortField() != null ? filterDto.getSortField() : "date";
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(filterDto.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortField);
        if ("income".equals(filterDto.getType())) {
            List<IncomeResponse> filterIncome = incomeService.filterIncome(startDate, endDate, keyword, sort);
            return ResponseEntity.ok().body(filterIncome);
        }
        if ("expense".equals(filterDto.getType())) {
            List<ExpenseResponse> expenses = expenseService.filterExpenses(startDate, endDate, keyword, sort);
            return ResponseEntity.ok().body(expenses);
        } else {
            return ResponseEntity.badRequest().body("Invalid filter type");
        }
    }

}
