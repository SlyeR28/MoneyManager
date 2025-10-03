package org.moneymanagement.Service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Payload.Response.ExpenseResponse;
import org.moneymanagement.Repository.ProfileRepository;
import org.moneymanagement.Service.ExpenseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

   @Value("${money.manager.fronend.url}")
    private String  frontendUrl;

//   @Scheduled(cron = "0 * * * * *" , zone = "IST")
   @Scheduled(cron = "0 0 22 * * *" , zone = "IST")
   public void sendDailyIncomeExpenseReminder(){
       log.info("Sending Daily Income Expense Reminder sendDailyIncomeExpenseReminder()");
       List<ProfileEntity> all = profileRepository.findAll();
        for(ProfileEntity profileEntity : all){
            String name = profileEntity.getFullName();
            String email = profileEntity.getEmail();

            // Compose email body
            String body = String.format(
                    """
                    Hello %s,
                    
                    This is your daily reminder to update your income and expenses for today.
                    
                    ✅ Keep your finances organized.
                    💰 Stay consistent.
                    📅 Track your daily progress.
                    
                    Best regards,
                    Money Management App
                    """,
                    name,
                    frontendUrl
            );

            try {
                emailService.sendEmail(email, "Daily Income & Expense Reminder", body);
                log.info("Reminder email sent successfully to: {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send reminder email to: {}", email, e);
            }
        }

       log.info("=== Daily Reminder Emails Sent to All Users ===");
        }



//    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata") // Runs daily at 11:00 PM IST
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Kolkata") // Runs daily at 11:00 PM IST
    public void sendDailyExpenseSummary() {

        log.info("=== Starting Daily Expense Summary Job ===");

        List<ProfileEntity> profiles = profileRepository.findAll();

        if (profiles.isEmpty()) {
            log.warn("⚠️ No user profiles found. Skipping daily expense summary job.");
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));

        for (ProfileEntity profile : profiles) {

            List<ExpenseResponse> todayExpenses =
                    expenseService.getExpensesByUserOnDate(profile.getId(), today);

            // Skip users with no expenses today
            if (todayExpenses.isEmpty()) {
                log.info("No expenses found today for user: {}", profile.getEmail());
                continue;
            }

            // Calculate total expenses
            BigDecimal total = todayExpenses.stream()
                    .map(ExpenseResponse::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Build expense breakdown by category
            StringBuilder expenseBreakdown = new StringBuilder();
            todayExpenses.forEach(expense -> expenseBreakdown.append(
                    String.format("• %s → ₹%s (%s)\n",
                            expense.getName(),
                            expense.getAmount(),
                            expense.getCategoryName())
            ));

            // Compose email body
            String body = String.format(
                    """
                    Hello %s,
                    
                    Here's your expense summary for %s:
                    
                    Total Spent: ₹%s
                    
                    Breakdown:
                    %s
                    
                    ✅ Keep tracking your expenses daily!
                    💰 Smart tracking = Smart saving.
                    
                    You can view your full expense report here:
                    %s
                    
                    Best regards,
                    Money Management App
                    """,
                    profile.getFullName(),
                    today,
                    total,
                    expenseBreakdown.toString(),
                    frontendUrl
            );

            try {
                emailService.sendEmail(
                        profile.getEmail(),
                        "Your Daily Expense Summary",
                        body
                );
                log.info("✅ Daily Expense Summary email sent to: {}", profile.getEmail());
            } catch (Exception e) {
                log.error("❌ Failed to send expense summary to: {}", profile.getEmail(), e);
            }
        }

        log.info("=== ✅ Daily Expense Summary Job Completed ===");
    }

}

