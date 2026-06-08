package com.example.rentlyauth.unit;



import com.example.rentlyauth.service.EmailService;
import com.example.rentlyauth.service.utils.EmailUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender javaMailSender;

    @InjectMocks
    EmailService emailService;

    private final String host = "http://test-host";
    private final String from = "no-reply@test.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "host", host);
        ReflectionTestUtils.setField(emailService, "from", from);
    }

    @Test
    void accountVerificationMessage_sendsEmailWithCorrectFields() {
        String name = "Alice";
        String to = "alice@example.com";
        String token = "token123";

        emailService.AccountVerificationMessage(name, to, token);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        assertThat(sent.getSubject()).isEqualTo("New User Account Verification");
        assertThat(sent.getTo()).containsExactly(to);
        assertThat(sent.getFrom()).isEqualTo(from);

        String expectedText = EmailUtils.getEmail(name, host, token);
        assertThat(sent.getText()).isEqualTo(expectedText);
    }

    @Test
    void accountVerificationMessage_whenMailSenderThrows_wrapsInRuntimeException() {
        String name = "Bob";
        String to = "bob@example.com";
        String token = "tok456";

        doThrow(new RuntimeException("SMTP error"))
                .when(javaMailSender).send(Mockito.any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.AccountVerificationMessage(name, to, token))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP error");
    }

    @Test
    void passwordResetMessage_sendsEmailWithCorrectFields() {
        String to = "charlie@example.com";
        String token = "reset789";

        emailService.PasswordResetMessage(to, token);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        assertThat(sent.getSubject()).isEqualTo("Password Reset");
        assertThat(sent.getTo()).containsExactly(to);
        assertThat(sent.getFrom()).isEqualTo(from);

        String expectedText = EmailUtils.getPassword(host, token);
        assertThat(sent.getText()).isEqualTo(expectedText);
    }

    @Test
    void passwordResetMessage_whenMailSenderThrows_wrapsInRuntimeException() {
        String to = "dave@example.com";
        String token = "reset000";
        doThrow(new RuntimeException("SMTP down"))
                .when(javaMailSender).send(Mockito.any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.PasswordResetMessage(to, token))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP down");
    }
}

