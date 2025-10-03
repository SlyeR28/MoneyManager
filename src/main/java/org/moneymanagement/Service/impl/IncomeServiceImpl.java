package org.moneymanagement.Service.impl;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Entity.Category;
import org.moneymanagement.Entity.Expense;
import org.moneymanagement.Entity.Income;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Mappers.IncomeMapper;
import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Payload.Request.IncomeRequest;
import org.moneymanagement.Payload.Response.IncomeResponse;
import org.moneymanagement.Repository.CategoryRepository;
import org.moneymanagement.Repository.IncomeRepository;
import org.moneymanagement.Service.IncomeService;
import org.moneymanagement.Service.ProfileService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;


    @Override
    public IncomeResponse addIncome(IncomeRequest incomeRequest) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Category category = categoryRepository.findById(incomeRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Income income = incomeMapper.requestToEntity(incomeRequest);
        income.setProfile(profile);
        income.setCategory(category);
        incomeRepository.save(income);
        return incomeMapper.entityToResponse(income);
    }

    @Override
    public IncomeResponse updateExpense(ExpenseRequest incomeRequest) {
        return null;
    }

    @Override
    public List<IncomeResponse> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate currentDate = LocalDate.now();
        LocalDate start = currentDate.withDayOfMonth(1);
        LocalDate end = currentDate.withDayOfMonth(currentDate.lengthOfMonth());
        List<Income> between = incomeRepository.findByProfileIdAndDateBetween
                (profile.getId(), start, end);

        return   between.stream().map(incomeMapper::entityToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteExpense(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Income income = incomeRepository.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));
        if(!income.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(income);
    }

    @Override
    public List<IncomeResponse> getLastest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<Income> orderByDateDesc = incomeRepository.findByProfileIdOrderByDateDesc(profile.getId());
       return orderByDateDesc.stream().map(incomeMapper::entityToResponse).collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalIncomesOfCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal income = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return income != null ? income : BigDecimal.ZERO;

    }

    @Override
    public List<IncomeResponse> filterIncome(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<Income> incomeList = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
       return incomeList.stream().map(incomeMapper::entityToResponse).collect(Collectors.toList());
    }
}
