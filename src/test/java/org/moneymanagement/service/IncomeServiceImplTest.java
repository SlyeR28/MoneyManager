package org.moneymanagement.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneymanagement.entity.Category;
import org.moneymanagement.entity.Income;
import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.mappers.IncomeMapper;
import org.moneymanagement.payload.request.IncomeRequest;
import org.moneymanagement.payload.response.IncomeResponse;
import org.moneymanagement.repository.CategoryRepository;
import org.moneymanagement.repository.IncomeRepository;
import org.moneymanagement.service.ProfileService;
import org.moneymanagement.service.impl.IncomeServiceImpl;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceImplTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private IncomeMapper incomeMapper;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    @Test
    @DisplayName("addIncome should save income and return response")
    void testAddIncome_Success() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Category category = Category.builder().id(2L).name("Freelance").build();

        IncomeRequest request = new IncomeRequest();
        request.setName("Web Design");
        request.setAmount(new BigDecimal("1200.00"));
        request.setCategoryId(2L);

        Income income = Income.builder().name("Web Design").amount(new BigDecimal("1200.00")).build();
        IncomeResponse response = IncomeResponse.builder().id(1L).name("Web Design").amount(new BigDecimal("1200.00")).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(incomeMapper.requestToEntity(request)).thenReturn(income);
        when(incomeRepository.save(income)).thenReturn(income);
        when(incomeMapper.entityToResponse(income)).thenReturn(response);

        IncomeResponse result = incomeService.addIncome(request);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("1200.00"));
        assertThat(income.getProfile()).isEqualTo(profile);
        assertThat(income.getCategory()).isEqualTo(category);
        verify(incomeRepository).save(income);
    }

    @Test
    @DisplayName("addIncome should throw RuntimeException when category is not found")
    void testAddIncome_CategoryNotFound() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        IncomeRequest request = new IncomeRequest();
        request.setCategoryId(99L);

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.addIncome(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");

        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCurrentMonthExpensesForCurrentUser should query current month date range")
    void testGetCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Income income = Income.builder().id(1L).name("Salary").build();
        IncomeResponse response = IncomeResponse.builder().id(1L).name("Salary").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeRepository.findByProfileIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(income));
        when(incomeMapper.entityToResponse(income)).thenReturn(response);

        List<IncomeResponse> list = incomeService.getCurrentMonthExpensesForCurrentUser();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("deleteExpense should delete income when owned by user")
    void testDeleteExpense_Success() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Income income = Income.builder().id(10L).profile(profile).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeRepository.findById(10L)).thenReturn(Optional.of(income));

        incomeService.deleteExpense(10L);

        verify(incomeRepository).delete(income);
    }

    @Test
    @DisplayName("deleteExpense should throw ResourceNotFoundException when income not found")
    void testDeleteExpense_NotFound() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.deleteExpense(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Income not found with id: 10");
    }

    @Test
    @DisplayName("deleteExpense should throw RuntimeException when user does not own the income")
    void testDeleteExpense_Unauthorized() {
        ProfileEntity currentProfile = ProfileEntity.builder().id(1L).build();
        ProfileEntity ownerProfile = ProfileEntity.builder().id(2L).build();
        Income income = Income.builder().id(10L).profile(ownerProfile).build();

        when(profileService.getCurrentProfile()).thenReturn(currentProfile);
        when(incomeRepository.findById(10L)).thenReturn(Optional.of(income));

        assertThatThrownBy(() -> incomeService.deleteExpense(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized to delete this income");

        verify(incomeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getLastest5ExpensesForCurrentUser should return mapped response list")
    void testGetLastest5ExpensesForCurrentUser() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Income income = Income.builder().id(1L).name("Bonus").build();
        IncomeResponse response = IncomeResponse.builder().id(1L).name("Bonus").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeRepository.findByProfileIdOrderByDateDesc(1L)).thenReturn(List.of(income));
        when(incomeMapper.entityToResponse(income)).thenReturn(response);

        List<IncomeResponse> list = incomeService.getLastest5ExpensesForCurrentUser();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Bonus");
    }

    @Test
    @DisplayName("getTotalIncomesOfCurrentUser should return total or BigDecimal.ZERO if null")
    void testGetTotalIncomesOfCurrentUser() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeRepository.findTotalIncomeByProfileId(1L)).thenReturn(new BigDecimal("5000.00"));

        BigDecimal total = incomeService.getTotalIncomesOfCurrentUser();
        assertThat(total).isEqualTo(new BigDecimal("5000.00"));

        when(incomeRepository.findTotalIncomeByProfileId(1L)).thenReturn(null);
        BigDecimal zeroTotal = incomeService.getTotalIncomesOfCurrentUser();
        assertThat(zeroTotal).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("filterIncome should query repository and return responses")
    void testFilterIncome() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "date");

        Income income = Income.builder().id(1L).name("Salary").build();
        IncomeResponse response = IncomeResponse.builder().id(1L).name("Salary").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(1L, start, end, "Sal", sort))
                .thenReturn(List.of(income));
        when(incomeMapper.entityToResponse(income)).thenReturn(response);

        List<IncomeResponse> list = incomeService.filterIncome(start, end, "Sal", sort);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Salary");
    }
}
