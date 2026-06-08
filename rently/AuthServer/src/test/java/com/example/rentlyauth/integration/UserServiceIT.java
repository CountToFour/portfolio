package com.example.rentlyauth.integration;

import com.example.rentlyauth.dto.RegisterRequest;
import com.example.rentlyauth.model.Confirmation;
import com.example.rentlyauth.model.PasswordResetConfirmation;
import com.example.rentlyauth.model.RolesEntity;
import com.example.rentlyauth.model.UsersEntity;
import com.example.rentlyauth.repository.ConfirmationRepository;
import com.example.rentlyauth.repository.PasswordResetConfirmationRepository;
import com.example.rentlyauth.repository.RoleRepository;
import com.example.rentlyauth.repository.UserRepository;
import com.example.rentlyauth.service.EmailService;
import com.example.rentlyauth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerJwtAutoConfiguration"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional
@Tag("integration")
public class UserServiceIT {
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }
    @Autowired
    UserRepository userRepository;
    @Autowired
    ConfirmationRepository confirmationRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @MockitoBean
    EmailService emailService;
    @Autowired
    PasswordResetConfirmationRepository passwordResetConfirmationRepository;


    @Autowired
    UserService userService;

    @MockitoBean
    JwtDecoder jwtDecoder;
    @MockitoBean
    AuthorizationServerSettings authorizationServerSettings;

    @MockitoBean(name = "authorizationServerSecurityFilterChain")
    private SecurityFilterChain authorizationServerSecurityFilterChain;

    @MockitoBean(name = "defaultSecurityFilterChain")
    private SecurityFilterChain defaultSecurityFilterChain;

    @MockitoBean
    private org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository registeredClientRepository;

    private RegisterRequest req;

    @TestConfiguration
    static class AuthorizationServerSettingsTestConfig {
        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {
            return AuthorizationServerSettings.builder()
                    .issuer("http://localhost")
                    .build();
        }
    }

    @BeforeEach
    void cleanDbAndPrepare() {

        passwordResetConfirmationRepository.deleteAll();
        confirmationRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        req = new RegisterRequest();
        req.setUsername("alice");
        req.setEmail("alice@example.com");
        req.setPassword("plainPass");
        req.setFirstName("Alicja");
        req.setLastName("Nowak");
    }

    // --- saveConfirmation ----------------------------------------------------------------

    @Test
    void saveConfirmation_existingRole_savesUserAndConfirmation() {
        RolesEntity role = new RolesEntity();
        role.setName("USER");
        role.setDescription("Default UserRole");
        roleRepository.save(role);

        userService.saveConfirmation(req);

        List<UsersEntity> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        UsersEntity u = users.get(0);

        assertThat(passwordEncoder.matches("plainPass", u.getPassword())).isTrue();
        assertThat(u.isEnabled()).isFalse();
        assertThat(u.isAccount_locked()).isTrue();
        assertThat(u.getRoles()).extracting("name").containsExactly("USER");

        List<Confirmation> confs = confirmationRepository.findAll();
        assertThat(confs).hasSize(1);
        Confirmation c = confs.get(0);
        assertThat(c.getUser().getId()).isEqualTo(u.getId());
        assertThat(c.getConfirmationCode()).isNotBlank();
    }
    @Test
    void saveConfirmation_noExistingRole_createsRole() {
        userService.saveConfirmation(req);
        Optional<RolesEntity> maybe = roleRepository.findByName("USER");
        assertThat(maybe).isPresent();
        RolesEntity created = maybe.get();
        assertThat(created.getDescription()).isNotBlank();

        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(confirmationRepository.findAll()).hasSize(1);
    }

    // ----------------------------
    // confirm tests
    // ----------------------------

    @Test
    void confirm_invalidToken_throwsRuntimeException() {
        assertThatThrownBy(() -> userService.confirm("bad"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid token");

        assertThat(userRepository.findAll()).isEmpty();
        assertThat(confirmationRepository.findAll()).isEmpty();
    }

    @Test
    void confirm_expiredToken_deletesUserAndConfirmationAndThrows() {
        UsersEntity u = new UsersEntity();
        u.setUsername("x");
        u.setEmail("x@x");
        u.setPassword("p");
        userRepository.save(u);

        Confirmation c = new Confirmation();
        c.setConfirmationCode("expired");
        c.setCreatedDate(LocalDateTime.now().minusMinutes(61));
        c.setUser(u);
        confirmationRepository.save(c);

        assertThatThrownBy(() -> userService.confirm("expired"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token expired");

        assertThat(userRepository.findAll()).isEmpty();
        assertThat(confirmationRepository.findAll()).isEmpty();
    }

    @Test
    void confirm_validToken_enablesUserAndCreatesProfile() {
        UsersEntity u = new UsersEntity();
        u.setUsername("alice");
        u.setEmail("alice@example.com");
        u.setPassword("p");
        u.setEnabled(false);
        u.setAccount_locked(true);
        userRepository.save(u);

        Confirmation c = new Confirmation();
        c.setConfirmationCode("good");
        c.setCreatedDate(LocalDateTime.now().minusMinutes(10));
        c.setUser(u);
        confirmationRepository.save(c);

        userService.confirm("good");

        UsersEntity saved = userRepository.findById(u.getId()).get();
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.isAccount_locked()).isFalse();

    }

    // ----------------------------
    // savePasswordReset tests
    // ----------------------------

    @Test
    void savePasswordReset_userNotFound_throwsRuntimeException() {
        assertThatThrownBy(() -> userService.savePasswordReset("no@user.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Podany mail nie istnieje");

        verify(emailService, never()).PasswordResetMessage(anyString(), anyString());
    }


    @Test
    void savePasswordReset_noExistingConfirmation_savesNewAndSendsEmail() {
        UsersEntity u = new UsersEntity();
        u.setEmail("carol@example.com");
        u.setUsername("carol");
        u.setPassword("p");
        userRepository.save(u);

        userService.savePasswordReset("carol@example.com");

        List<PasswordResetConfirmation> all = passwordResetConfirmationRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getUser().getEmail()).isEqualTo("carol@example.com");

        verify(emailService).PasswordResetMessage(eq("carol@example.com"), anyString());
    }

    // ----------------------------
    // confirmPasswordReset tests
    // ----------------------------

    @Test
    void confirmPasswordReset_invalidToken_throwsRuntimeException() {
        assertThatThrownBy(() -> userService.confirmPasswordReset("bad", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void confirmPasswordReset_expiredToken_deletesAndThrows() {
        UsersEntity u = new UsersEntity();
        u.setUsername("u");
        u.setEmail("u@u");
        u.setPassword("p");
        userRepository.save(u);

        PasswordResetConfirmation prc = new PasswordResetConfirmation();
        prc.setToken("expired");
        prc.setCreated(LocalDateTime.now().minusMinutes(61));
        prc.setUser(u);
        passwordResetConfirmationRepository.save(prc);

        assertThatThrownBy(() -> userService.confirmPasswordReset("expired", "new"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token expired");

        assertThat(passwordResetConfirmationRepository.findAll()).isEmpty();
    }

    @Test
    void confirmPasswordReset_validToken_updatesPasswordAndDeletesConfirmation() {
        UsersEntity u = new UsersEntity();
        u.setUsername("u2");
        u.setEmail("u2@u");
        u.setPassword("old");
        userRepository.save(u);

        PasswordResetConfirmation prc = new PasswordResetConfirmation();
        prc.setToken("good");
        prc.setCreated(LocalDateTime.now().minusMinutes(10));
        prc.setUser(u);
        passwordResetConfirmationRepository.save(prc);

        userService.confirmPasswordReset("good", "newPass");

        UsersEntity saved = userRepository.findById(u.getId()).get();
        assertThat(passwordEncoder.matches("newPass", saved.getPassword())).isTrue();

        assertThat(passwordResetConfirmationRepository.findAll()).isEmpty();
    }



}

