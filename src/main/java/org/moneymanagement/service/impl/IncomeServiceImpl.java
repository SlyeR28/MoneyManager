package org.moneymanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.entity.Category;
import org.moneymanagement.entity.Income;
import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.mappers.IncomeMapper;
import org.moneymanagement.payload.request.ExpenseRequest;
import org.moneymanagement.payload.request.IncomeRequest;
import org.moneymanagement.payload.response.IncomeResponse;
import org.moneymanagement.repository.CategoryRepository;
import org.moneymanagement.repository.IncomeRepository;
import org.moneymanagement.service.IncomeService;
import org.moneymanagement.service.ProfileService;
import org.moneymanagement.exception.ResourceNotFoundException;
import org.moneymanagement.exception.UnauthorizedException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + incomeRequest.getCategoryId()));
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

        return   between.stream().map(incomeMapper::entityToResponse).toList();
    }

    @Override
    public void deleteExpense(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Income income = incomeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));
        if(!income.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedException("Unauthorized to delete this income");
        }
        incomeRepository.delete(income);
    }


    @Override
    public List<IncomeResponse> getLastest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<Income> orderByDateDesc = incomeRepository.findByProfileIdOrderByDateDesc(profile.getId());
       return orderByDateDesc.stream().map(incomeMapper::entityToResponse).toList();
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
       return incomeList.stream().map(incomeMapper::entityToResponse).toList();
    }
}
