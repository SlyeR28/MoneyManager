package org.moneymanagement.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moneymanagement.Entity.Category;
import org.moneymanagement.Entity.Expense;
import org.moneymanagement.Entity.Income;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Repository.CategoryRepository;
import org.moneymanagement.Repository.ExpenseRepository;
import org.moneymanagement.Repository.IncomeRepository;
import org.moneymanagement.Repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;

@Slf4j
@Component
@Profile({"docker", "prod"})
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seeder.default.password:MoneyMgr#SecureSeed2026!}")
    private String seederDefaultPassword;

    @Override
    @Transactional
    public void run(String... args) {
        // Safe check: Only seed if no profiles exist yet
        if (profileRepository.count() > 0) {
            log.info("⚡ Profiles already exist in database. Skipping DatabaseSeeder.");
            return;
        }

        log.info("🌱 Starting database seeding for Docker/Prod environment...");

        // 1. Create 5 Profiles
        List<ProfileEntity> profiles = createProfiles();

        // 2. For each profile, create Categories, Incomes, and 30-40 Expenses
        for (ProfileEntity profile : profiles) {
            seedProfileData(profile);
        }

        log.info("✅ Database seeding completed successfully! Seeded 5 profiles with full categories, incomes, and expenses.");
    }

    private List<ProfileEntity> createProfiles() {
        String defaultPassword = passwordEncoder.encode(seederDefaultPassword);

        List<ProfileEntity> profiles = List.of(
            ProfileEntity.builder()
                .fullName("Alex Morgan")
                .email("alex@moneymanager.com")
                .password(defaultPassword)
                .isActive(true)
                .build(),
            ProfileEntity.builder()
                .fullName("Sarah Jenkins")
                .email("sarah@moneymanager.com")
                .password(defaultPassword)
                .isActive(true)
                .build(),
            ProfileEntity.builder()
                .fullName("David Miller")
                .email("david@moneymanager.com")
                .password(defaultPassword)
                .isActive(true)
                .build(),
            ProfileEntity.builder()
                .fullName("Emma Watson")
                .email("emma@moneymanager.com")
                .password(defaultPassword)
                .isActive(true)
                .build(),
            ProfileEntity.builder()
                .fullName("Admin User")
                .email("admin@moneymanager.com")
                .password(defaultPassword)
                .isActive(true)
                .build()
        );

        return profileRepository.saveAll(profiles);
    }

    private void seedProfileData(ProfileEntity profile) {
        String userTag = profile.getFullName();

        // 1. Seed Categories (Names are unique across the database)
        Category salaryCat = categoryRepository.save(Category.builder()
                .name("Salary - " + userTag)
                .type("INCOME")
                .icon("briefcase")
                .profile(profile)
                .build());

        Category freelanceCat = categoryRepository.save(Category.builder()
                .name("Freelance - " + userTag)
                .type("INCOME")
                .icon("laptop")
                .profile(profile)
                .build());

        Category investCat = categoryRepository.save(Category.builder()
                .name("Investments - " + userTag)
                .type("INCOME")
                .icon("trending-up")
                .profile(profile)
                .build());

        Category foodCat = categoryRepository.save(Category.builder()
                .name("Food & Dining - " + userTag)
                .type("EXPENSE")
                .icon("utensils")
                .profile(profile)
                .build());

        Category groceriesCat = categoryRepository.save(Category.builder()
                .name("Groceries - " + userTag)
                .type("EXPENSE")
                .icon("shopping-cart")
                .profile(profile)
                .build());

        Category rentCat = categoryRepository.save(Category.builder()
                .name("Rent & Housing - " + userTag)
                .type("EXPENSE")
                .icon("home")
                .profile(profile)
                .build());

        Category utilitiesCat = categoryRepository.save(Category.builder()
                .name("Utilities - " + userTag)
                .type("EXPENSE")
                .icon("zap")
                .profile(profile)
                .build());

        Category entertainmentCat = categoryRepository.save(Category.builder()
                .name("Entertainment - " + userTag)
                .type("EXPENSE")
                .icon("film")
                .profile(profile)
                .build());

        Category healthCat = categoryRepository.save(Category.builder()
                .name("Healthcare - " + userTag)
                .type("EXPENSE")
                .icon("heart")
                .profile(profile)
                .build());

        Category travelCat = categoryRepository.save(Category.builder()
                .name("Transportation - " + userTag)
                .type("EXPENSE")
                .icon("truck")
                .profile(profile)
                .build());

        // 2. Seed Incomes
        incomeRepository.save(Income.builder()
                .name("Monthly Salary (Last Month) - " + userTag)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(35))
                .category(salaryCat)
                .profile(profile)
                .build());

        incomeRepository.save(Income.builder()
                .name("Monthly Salary (Current Month) - " + userTag)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(5))
                .category(salaryCat)
                .profile(profile)
                .build());

        incomeRepository.save(Income.builder()
                .name("Consulting Project - " + userTag)
                .amount(new BigDecimal("1250.00"))
                .date(LocalDate.now().minusDays(18))
                .category(freelanceCat)
                .profile(profile)
                .build());

        incomeRepository.save(Income.builder()
                .name("Stock Dividend - " + userTag)
                .amount(new BigDecimal("320.50"))
                .date(LocalDate.now().minusDays(12))
                .category(investCat)
                .profile(profile)
                .build());

        // 3. Seed 35 Expenses per Profile
        List<Expense> expenses = new ArrayList<>();
        Category[] expCategories = {foodCat, groceriesCat, rentCat, utilitiesCat, entertainmentCat, healthCat, travelCat};

        String[] expenseTemplates = {
            "Supermarket Groceries", "Starbucks Coffee", "Electricity Bill", "High-Speed Internet",
            "Netflix Subscription", "Uber Rides", "Dinner with Friends", "Pharmacy Medicines",
            "Gym Membership", "Amazon Shopping", "Fuel & Gas Refill", "Water Utility Bill",
            "Cinema Movie Night", "Weekend Brunch", "Spotify Premium", "Vehicle Service",
            "Technical Book Store", "Dentist Consultation", "Mobile Phone Plan", "Household Supplies",
            "Bakery & Snacks", "Clothing Store", "Cleaning Services", "Electronics Gadgets"
        };

        SecureRandom random = new SecureRandom();

        for (int i = 1; i <= 35; i++) {
            Category category = expCategories[random.nextInt(expCategories.length)];
            String templateName = expenseTemplates[random.nextInt(expenseTemplates.length)];
            String expenseTitle = String.format("%s #%02d - %s", templateName, i, userTag);

            double randomAmount = 12.0 + (random.nextDouble() * 185.0);
            BigDecimal amount = BigDecimal.valueOf(randomAmount).setScale(2, RoundingMode.HALF_UP);
            LocalDate expenseDate = LocalDate.now().minusDays(random.nextInt(55));

            expenses.add(Expense.builder()
                    .name(expenseTitle)
                    .amount(amount)
                    .date(expenseDate)
                    .category(category)
                    .profile(profile)
                    .build());
        }

        expenseRepository.saveAll(expenses);
    }
}
