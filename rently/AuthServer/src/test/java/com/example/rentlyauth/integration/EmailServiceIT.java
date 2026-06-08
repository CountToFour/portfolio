package com.example.rentlyauth.integration;

import com.example.rentlyauth.service.EmailService;
import com.example.rentlyauth.service.utils.EmailUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.PrintStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerJwtAutoConfiguration"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(EmailServiceIT.SyncExecutorConfig.class)
@Tag("integration")
class EmailServiceIT {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean(name = "authorizationServerSecurityFilterChain")
    private SecurityFilterChain authorizationServerSecurityFilterChain;

    @MockitoBean(name = "defaultSecurityFilterChain")
    private SecurityFilterChain defaultSecurityFilterChain;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthorizationServerSettings authorizationServerSettings;

    private final String host = "http://test-host";
    private final String from = "no-reply@test.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "host", host);
        ReflectionTestUtils.setField(emailService, "from", from);
    }
    @TestConfiguration
    static class SyncExecutorConfig {
        @Bean(name = "taskExecutor")
        public org.springframework.core.task.TaskExecutor taskExecutor() {
            return new org.springframework.core.task.SyncTaskExecutor();
        }
    }


    @Test
    void accountVerificationMessage_sendsEmailWithCorrectFields() {
        String name = "Alice";
        String to = "alice@example.com";
        String token = "token123";

        emailService.AccountVerificationMessage(name, to, token);

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
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
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            String name = "Bob";
            String to = "bob@example.com";
            String token = "tok456";

            doThrow(new RuntimeException("SMTP error"))
                    .when(javaMailSender).send(any(SimpleMailMessage.class));

            emailService.AccountVerificationMessage(name, to, token);

            assertThat(outputStream.toString()).contains("SMTP error");

        } finally {
            System.setOut(originalOut);
        }
    }



    @Test
    void passwordResetMessage_sendsEmailWithCorrectFields() {
        String to = "charlie@example.com";
        String token = "reset789";

        emailService.PasswordResetMessage(to, token);

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
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
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        try {
            String to = "dave@example.com";
            String token = "reset000";

            doThrow(new RuntimeException("SMTP down"))
                    .when(javaMailSender).send(any(SimpleMailMessage.class));

            emailService.PasswordResetMessage(to, token);

            assertThat(outputStream.toString()).contains("SMTP down");

        } finally {
            System.setOut(originalOut);
        }
    }
}
