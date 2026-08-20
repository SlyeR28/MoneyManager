package org.moneymanagement.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneymanagement.Entity.Category;
import org.moneymanagement.Entity.Expense;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Mappers.ExpenseMapper;
import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Payload.Response.ExpenseResponse;
import org.moneymanagement.Repository.CategoryRepository;
import org.moneymanagement.Repository.ExpenseRepository;
import org.moneymanagement.Service.ProfileService;
import org.moneymanagement.Service.impl.ExpenseServiceImpl;
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
class ExpenseServiceImplTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    @DisplayName("addExpense should save expense and return response")
    void testAddExpense_Success() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Category category = Category.builder().id(3L).name("Food").build();

        ExpenseRequest request = new ExpenseRequest();
        request.setName("Dinner");
        request.setAmount(new BigDecimal("45.00"));
        request.setCategoryId(3L);

        Expense expense = Expense.builder().name("Dinner").amount(new BigDecimal("45.00")).build();
        ExpenseResponse response = ExpenseResponse.builder().id(1L).name("Dinner").amount(new BigDecimal("45.00")).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(expenseMapper.requestToEntity(request)).thenReturn(expense);
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(expenseMapper.entityToResponse(expense)).thenReturn(response);

        ExpenseResponse result = expenseService.addExpense(request);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("45.00"));
        assertThat(expense.getProfile()).isEqualTo(profile);
        assertThat(expense.getCategory()).isEqualTo(category);
        verify(expenseRepository).save(expense);
    }

    @Test
    @DisplayName("addExpense should throw RuntimeException when category is not found")
    void testAddExpense_CategoryNotFound() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        ExpenseRequest request = new ExpenseRequest();
        request.setCategoryId(99L);

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.addExpense(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCurrentMonthExpensesForCurrentUser should query current month date range")
    void testGetCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Expense expense = Expense.builder().id(1L).name("Grocery").build();
        ExpenseResponse response = ExpenseResponse.builder().id(1L).name("Grocery").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(expenseRepository.findByProfileIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(expense));
        when(expenseMapper.entityToResponse(expense)).thenReturn(response);

        List<ExpenseResponse> list = expenseService.getCurrentMonthExpensesForCurrentUser();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Grocery");
    }

    @Test
    @DisplayName("deleteExpense should delete expense when owned by user")
    void testDeleteExpense_Success() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Expense expense = Expense.builder().id(10L).profile(profile).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(10L);

        verify(expenseRepository).delete(expense);
    }

    @Test
    @DisplayName("deleteExpense should throw RuntimeException when expense not found")
    void testDeleteExpense_NotFound() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(expenseRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found");
    }

    @Test
    @DisplayName("deleteExpense should throw RuntimeException when user does not own the expense")
    void testDeleteExpense_Unauthorized() {
        ProfileEntity currentProfile = ProfileEntity.builder().id(1L).build();
        ProfileEntity ownerProfile = ProfileEntity.builder().id(2L).build();
        Expense expense = Expense.builder().id(10L).profile(ownerProfile).build();

        when(profileService.getCurrentProfile()).thenReturn(currentProfile);
        when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.deleteExpense(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized to delete this expense");

        verify(expenseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getLastest5ExpensesForCurrentUser should return mapped response list")
    void testGetLastest5ExpensesForCurrentUser() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Expense expense = Expense.builder().id(1L).name("Coffee").build();
        ExpenseResponse response = ExpenseResponse.builder().id(1L).name("Coffee").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(expenseRepository.findTop5ByProfileIdOrderByDateDesc(1L)).thenReturn(List.of(expense));
        when(expenseMapper.entityToResponse(expense)).thenReturn(response);

        List<ExpenseResponse> list = expenseService.getLastest5ExpensesForCurrentUser();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Coffee");
    }

    @Test
    @DisplayName("getTotalExpensesOfCurrentUser should return total or BigDecimal.ZERO if null")
    void testGetTotalExpensesOfCurrentUser() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(expenseRepository.findTotalExpenseByProfileId(1L)).thenReturn(new BigDecimal("350.00"));

        BigDecimal total = expenseService.getTotalExpensesOfCurrentUser();
        assertThat(total).isEqualTo(new BigDecimal("350.00"));

        when(expenseRepository.findTotalExpenseByProfileId(1L)).thenReturn(null);
        BigDecimal zeroTotal = expenseService.getTotalExpensesOfCurrentUser();
        assertThat(zeroTotal).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("filterExpenses should query repository and return responses")
    void testFilterExpenses() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "date");

        Expense expense = Expense.builder().id(1L).name("Fuel").build();
        ExpenseResponse response = ExpenseResponse.builder().id(1L).name("Fuel").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(1L, start, end, "Fuel", sort))
                .thenReturn(List.of(expense));
        when(expenseMapper.entityToResponse(expense)).thenReturn(response);

        List<ExpenseResponse> list = expenseService.filterExpenses(start, end, "Fuel", sort);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Fuel");
    }

    @Test
    @DisplayName("getExpensesByUserOnDate should return list for specific user and date")
    void testGetExpensesByUserOnDate() {
        Expense expense = Expense.builder().id(1L).name("Lunch").build();
        ExpenseResponse response = ExpenseResponse.builder().id(1L).name("Lunch").build();

        when(expenseRepository.findByProfileIdAndDate(1L, LocalDate.now())).thenReturn(List.of(expense));
        when(expenseMapper.entityToResponse(expense)).thenReturn(response);

        List<ExpenseResponse> list = expenseService.getExpensesByUserOnDate(1L, LocalDate.now());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Lunch");
    }
}
