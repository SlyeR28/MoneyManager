package org.moneymanagement.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moneymanagement.entity.Category;
import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.repository.CategoryRepository;
import org.moneymanagement.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    @DisplayName("Should find all categories for a given profile ID")
    void testFindByProfileId() {
        List<Category> categories = categoryRepository.findByProfileId(1L);

        assertThat(categories).isNotEmpty();
        assertThat(categories.size()).isGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("Should find category by ID and Profile ID")
    void testFindByIdAndProfileId() {
        Optional<Category> category = categoryRepository.findByIdAndProfileId(1L, 1L);

        assertThat(category).isPresent();
        assertThat(category.get().getName()).isEqualTo("Salary");
    }

    @Test
    @DisplayName("Should return empty when category does not belong to profile")
    void testFindByIdAndProfileId_NotMatching() {
        Optional<Category> category = categoryRepository.findByIdAndProfileId(1L, 999L);

        assertThat(category).isEmpty();
    }

    @Test
    @DisplayName("Should find categories by Type and Profile ID")
    void testFindByTypeAndProfileId() {
        List<Category> incomeCategories = categoryRepository.findByTypeAndProfileId("income", 1L);

        assertThat(incomeCategories).hasSizeGreaterThanOrEqualTo(2);
        assertThat(incomeCategories).allMatch(c -> "income".equalsIgnoreCase(c.getType()));

        List<Category> expenseCategories = categoryRepository.findByTypeAndProfileId("expense", 1L);
        assertThat(expenseCategories).hasSizeGreaterThanOrEqualTo(2);
        assertThat(expenseCategories).allMatch(c -> "expense".equalsIgnoreCase(c.getType()));
    }

    @Test
    @DisplayName("Should check if category exists by Name and Profile ID")
    void testExistsByNameAndProfileId() {
        Boolean exists = categoryRepository.existsByNameAndProfileId("Salary", 1L);
        assertThat(exists).isTrue();

        Boolean notExists = categoryRepository.existsByNameAndProfileId("Investment", 1L);
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should save new category for profile")
    void testSaveCategory() {
        ProfileEntity profile = profileRepository.findById(1L).orElseThrow();

        Category category = Category.builder()
                .name("Bonus")
                .type("income")
                .icon("gift")
                .profile(profile)
                .build();

        Category saved = categoryRepository.save(category);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Bonus");
    }
}
