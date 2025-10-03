package org.moneymanagement.Service.impl;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Payload.Request.RecentTransactionDto;
import org.moneymanagement.Payload.Response.ExpenseResponse;
import org.moneymanagement.Payload.Response.IncomeResponse;
import org.moneymanagement.Service.ExpenseService;
import org.moneymanagement.Service.IncomeService;
import org.moneymanagement.Service.ProfileService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashBoardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String , Object> getDashBoardData(){
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String , Object> map = new LinkedHashMap<>();
        List<IncomeResponse> latestIncomes = incomeService.getLastest5ExpensesForCurrentUser();
        List<ExpenseResponse> latestExpenses = expenseService.getLastest5ExpensesForCurrentUser();
        List<RecentTransactionDto> recentTransaction = concat(latestIncomes.stream().map(income -> RecentTransactionDto.builder()
                        .id(income.getId())
                        .profileId(profile.getId())
                        .icon(income.getIcon())
                        .name(income.getName())
                        .amount(income.getAmount())
                        .date(income.getDate())
                        .createdAt(income.getCreationAt())
                        .updatedAt(income.getUpdatedAt())
                        .type("INCOME")
                        .build()),
                latestExpenses.stream().map(expense -> RecentTransactionDto.builder()
                        .id(expense.getId())
                        .profileId(profile.getId())
                        .icon(expense.getIcon())
                        .name(expense.getName())
                        .amount(expense.getAmount())
                        .date(expense.getDate())
                        .createdAt(expense.getCreationAt())
                        .updatedAt(expense.getUpdatedAt())
                        .type("EXPENSE")
                        .build())).sorted(
                (a , b) ->{
                    int cmp = b.getDate().compareTo(a.getDate());
                    if(cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                }

        ).collect(Collectors.toList());

        map.put("Total Balance" , incomeService.getTotalIncomesOfCurrentUser()
                .subtract(expenseService.getTotalExpensesOfCurrentUser()));
        map.put("Total Income", incomeService.getTotalIncomesOfCurrentUser());
        map.put("Total Expense", expenseService.getTotalExpensesOfCurrentUser());
        map.put("recent5Expenses", latestExpenses);
        map.put("recent5Incomes" , latestIncomes);
        map.put("recentTransaction", recentTransaction);
        return map;


    }

}
