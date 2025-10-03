package org.moneymanagement.Service.impl;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Entity.Category;
import org.moneymanagement.Entity.Expense;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Mappers.ExpenseMapper;
import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Payload.Response.ExpenseResponse;
import org.moneymanagement.Repository.CategoryRepository;
import org.moneymanagement.Repository.ExpenseRepository;
import org.moneymanagement.Service.ExpenseService;
import org.moneymanagement.Service.ProfileService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ProfileService  profileService;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper  expenseMapper;


    @Override
    public ExpenseResponse addExpense(ExpenseRequest expenseRequest) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(expenseRequest.getCategoryId()).
                orElseThrow(() -> new RuntimeException("Category not found"));
        Expense expense = expenseMapper.requestToEntity(expenseRequest);
        expense.setProfile(profile);
        expense.setCategory(category);
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.entityToResponse(saved);

    }

    @Override
    public ExpenseResponse updateExpense(ExpenseRequest expenseRequest) {
        return null;
    }

    @Override
    public List<ExpenseResponse> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate currentDate = LocalDate.now();
        LocalDate start = currentDate.withDayOfMonth(1);
        LocalDate end = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        List<Expense> between = expenseRepository.findByProfileIdAndDateBetween
                (profile.getId(), start, end);

       return   between.stream().map(expenseMapper::entityToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteExpense(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
       if(!expense.getProfile().getId().equals(profile.getId())) {
           throw new RuntimeException("Unauthorized to delete this expense");
       }

         expenseRepository.delete(expense);
    }

    @Override
    public List<ExpenseResponse> getLastest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<Expense> orderByDateDesc = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return  orderByDateDesc.stream().map(expenseMapper::entityToResponse).collect(Collectors.toList());

    }

    @Override
    public BigDecimal getTotalExpensesOfCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal expense = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return expense != null ? expense : BigDecimal.ZERO;
    }

    @Override
    public List<ExpenseResponse> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<Expense> expenseList = expenseRepository.
                findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return  expenseList.stream().map(expenseMapper::entityToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseResponse> getExpensesByUserOnDate(Long profileId, LocalDate date) {
        List<Expense> expenseList = expenseRepository.findByProfileIdAndDate(profileId, date);
        return  expenseList.stream().map(expenseMapper::entityToResponse).collect(Collectors.toList());
    }
}
