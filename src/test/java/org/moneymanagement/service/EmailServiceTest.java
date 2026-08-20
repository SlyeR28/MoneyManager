package org.moneymanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneymanagement.Service.impl.EmailService;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@moneymanager.com");
    }

    @Test
    @DisplayName("sendEmail should compose and send SimpleMailMessage")
    void testSendEmail_Success() {
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendEmail("user@example.com", "Test Subject", "Test Body");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sent = messageCaptor.getValue();
        assertThat(sent.getTo()).containsExactly("user@example.com");
        assertThat(sent.getSubject()).isEqualTo("Test Subject");
        assertThat(sent.getText()).isEqualTo("Test Body");
        assertThat(sent.getFrom()).isEqualTo("noreply@moneymanager.com");
    }

    @Test
    @DisplayName("sendEmail should wrap exception in RuntimeException when mailSender fails")
    void testSendEmail_ThrowsException() {
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendEmail("user@example.com", "Subject", "Body"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP error");
    }
}
