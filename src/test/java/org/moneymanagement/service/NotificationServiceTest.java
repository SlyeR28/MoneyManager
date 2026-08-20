package org.moneymanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Payload.Response.ExpenseResponse;
import org.moneymanagement.Repository.ProfileRepository;
import org.moneymanagement.Service.ExpenseService;
import org.moneymanagement.Service.impl.EmailService;
import org.moneymanagement.Service.impl.NotificationService;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "frontendUrl", "http://localhost:5173");
    }

    @Test
    @DisplayName("sendDailyIncomeExpenseReminder should iterate over all users and send reminder emails")
    void testSendDailyIncomeExpenseReminder() {
        ProfileEntity user1 = ProfileEntity.builder().fullName("Alice").email("alice@example.com").build();
        ProfileEntity user2 = ProfileEntity.builder().fullName("Bob").email("bob@example.com").build();

        when(profileRepository.findAll()).thenReturn(List.of(user1, user2));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        notificationService.sendDailyIncomeExpenseReminder();

        verify(emailService).sendEmail(eq("alice@example.com"), eq("Daily Income & Expense Reminder"), contains("Alice"));
        verify(emailService).sendEmail(eq("bob@example.com"), eq("Daily Income & Expense Reminder"), contains("Bob"));
    }

    @Test
    @DisplayName("sendDailyExpenseSummary should skip when no profiles exist")
    void testSendDailyExpenseSummary_NoProfiles() {
        when(profileRepository.findAll()).thenReturn(Collections.emptyList());

        notificationService.sendDailyExpenseSummary();

        verify(expenseService, never()).getExpensesByUserOnDate(any(), any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    @DisplayName("sendDailyExpenseSummary should send summary email when user has expenses today")
    void testSendDailyExpenseSummary_WithExpenses() {
        ProfileEntity user = ProfileEntity.builder().id(1L).fullName("Alice").email("alice@example.com").build();
        ExpenseResponse expense = ExpenseResponse.builder()
                .name("Coffee")
                .amount(new BigDecimal("5.00"))
                .categoryName("Food")
                .build();

        when(profileRepository.findAll()).thenReturn(List.of(user));
        when(expenseService.getExpensesByUserOnDate(eq(1L), any(LocalDate.class))).thenReturn(List.of(expense));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        notificationService.sendDailyExpenseSummary();

        verify(emailService).sendEmail(eq("alice@example.com"), eq("Your Daily Expense Summary"), contains("Coffee"));
    }

    @Test
    @DisplayName("sendDailyExpenseSummary should skip email when user has no expenses today")
    void testSendDailyExpenseSummary_NoExpensesToday() {
        ProfileEntity user = ProfileEntity.builder().id(1L).fullName("Alice").email("alice@example.com").build();

        when(profileRepository.findAll()).thenReturn(List.of(user));
        when(expenseService.getExpensesByUserOnDate(eq(1L), any(LocalDate.class))).thenReturn(Collections.emptyList());

        notificationService.sendDailyExpenseSummary();

        verify(emailService, never()).sendEmail(any(), any(), any());
    }
}
