package org.moneymanagement.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.Entity.Category;
import org.moneymanagement.Entity.Income;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Repository.CategoryRepository;
import org.moneymanagement.Repository.IncomeRepository;
import org.moneymanagement.Repository.ProfileRepository;
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
class IncomeRepositoryTest {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should find incomes ordered by date descending")
    void testFindByProfileIdOrderByDateDesc() {
        List<Income> incomes = incomeRepository.findByProfileIdOrderByDateDesc(1L);

        assertThat(incomes).isNotEmpty();
        assertThat(incomes.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should find top 5 incomes for profile ordered by date desc")
    void testFindTop5ByProfileIdOrderByDateDesc() {
        List<Income> top5 = incomeRepository.findTop5ByProfileIdOrderByDateDesc(1L);

        assertThat(top5).isNotEmpty();
        assertThat(top5.size()).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should calculate total income for profile")
    void testFindTotalIncomeByProfileId() {
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(1L);

        assertThat(total).isNotNull();
        assertThat(total).isGreaterThanOrEqualTo(new BigDecimal("6500.00"));
    }

    @Test
    @DisplayName("Should filter incomes by date range and keyword")
    void testFilterIncomes() {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().plusDays(30);

        List<Income> filtered = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                1L, start, end, "Salary", Sort.by(Sort.Direction.DESC, "date")
        );

        assertThat(filtered).isNotEmpty();
        assertThat(filtered.get(0).getName()).containsIgnoringCase("Salary");
    }

    @Test
    @DisplayName("Should find incomes between start and end date")
    void testFindByProfileIdAndDateBetween() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now().plusDays(5);

        List<Income> incomes = incomeRepository.findByProfileIdAndDateBetween(1L, start, end);

        assertThat(incomes).isNotEmpty();
    }

    @Test
    @DisplayName("Should save and delete income")
    void testSaveAndDeleteIncome() {
        ProfileEntity profile = profileRepository.findById(1L).orElseThrow();
        Category category = categoryRepository.findById(1L).orElseThrow();

        Income income = Income.builder()
                .name("Dividend Income")
                .amount(new BigDecimal("300.00"))
                .icon("chart")
                .date(LocalDate.now())
                .profile(profile)
                .category(category)
                .build();

        Income saved = incomeRepository.save(income);
        assertThat(saved.getId()).isNotNull();

        incomeRepository.delete(saved);
        assertThat(incomeRepository.findById(saved.getId())).isEmpty();
    }
}
