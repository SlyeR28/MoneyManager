package org.moneymanagement.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.entity.Category;
import org.moneymanagement.entity.Expense;
import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.repository.CategoryRepository;
import org.moneymanagement.repository.ExpenseRepository;
import org.moneymanagement.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should find expenses ordered by date descending")
    void testFindByProfileIdOrderByDateDesc() {
        List<Expense> expenses = expenseRepository.findByProfileIdOrderByDateDesc(1L);

        assertThat(expenses).isNotEmpty();
        assertThat(expenses.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should find top 5 expenses for profile ordered by date desc")
    void testFindTop5ByProfileIdOrderByDateDesc() {
        List<Expense> top5 = expenseRepository.findTop5ByProfileIdOrderByDateDesc(1L);

        assertThat(top5).isNotEmpty();
        assertThat(top5.size()).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should calculate total expense for profile")
    void testFindTotalExpenseByProfileId() {
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(1L);

        assertThat(total).isNotNull();
        assertThat(total).isGreaterThanOrEqualTo(new BigDecimal("370.00"));
    }

    @Test
    @DisplayName("Should filter expenses by date range and keyword")
    void testFilterExpenses() {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().plusDays(30);

        List<Expense> filtered = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                1L, start, end, "Shopping", Sort.by(Sort.Direction.DESC, "date")
        );

        assertThat(filtered).isNotEmpty();
        assertThat(filtered.get(0).getName()).containsIgnoringCase("Shopping");
    }

    @Test
    @DisplayName("Should find expenses between start and end date")
    void testFindByProfileIdAndDateBetween() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now().plusDays(5);

        List<Expense> expenses = expenseRepository.findByProfileIdAndDateBetween(1L, start, end);

        assertThat(expenses).isNotEmpty();
    }

    @Test
    @DisplayName("Should find expenses by profile ID and specific date")
    void testFindByProfileIdAndDate() {
        List<Expense> expenses = expenseRepository.findByProfileIdAndDate(1L, LocalDate.now());

        assertThat(expenses).isNotEmpty();
    }

    @Test
    @DisplayName("Should save and delete expense")
    void testSaveAndDeleteExpense() {
        ProfileEntity profile = profileRepository.findById(1L).orElseThrow();
        Category category = categoryRepository.findById(3L).orElseThrow();

        Expense expense = Expense.builder()
                .name("Dining Out")
                .amount(new BigDecimal("85.50"))
                .icon("utensils")
                .date(LocalDate.now())
                .profile(profile)
                .category(category)
                .build();

        Expense saved = expenseRepository.save(expense);
        assertThat(saved.getId()).isNotNull();

        expenseRepository.delete(saved);
        assertThat(expenseRepository.findById(saved.getId())).isEmpty();
    }
}
