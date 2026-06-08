package com.example.rentlyauth.unit;


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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ConfirmationRepository confirmationRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock
    EmailService emailService;
    @Mock
    PasswordResetConfirmationRepository passwordResetConfirmationRepository;

    @InjectMocks
    UserService userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("alice");
        registerRequest.setEmail("alice@example.com");
        registerRequest.setPassword("plainPass");
        registerRequest.setFirstName("Alicja");
        registerRequest.setLastName("Nowak");
    }

    // ----------------------------
    // saveConfirmation tests
    // ----------------------------

    @Test
    void saveConfirmation_existingRole_savesUserAndConfirmationAndSendsEmail() {
        RolesEntity existingRole = new RolesEntity();
        existingRole.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(existingRole));

        when(passwordEncoder.encode("plainPass")).thenReturn("encodedPass");

        UsersEntity savedUser = new UsersEntity();
        savedUser.setId(10L);
        savedUser.setUsername("alice");
        savedUser.setEmail("alice@example.com");
        savedUser.setPassword("encodedPass");
        savedUser.setFirstName("Alicja");
        savedUser.setLastName("Nowak");
        savedUser.setEnabled(false);
        savedUser.setAccount_expired(false);
        savedUser.setAccount_locked(true);
        savedUser.setCredentials_expired(false);
        savedUser.setRoles(Collections.singleton(existingRole));
        when(userRepository.save(any(UsersEntity.class))).thenReturn(savedUser);

        userService.saveConfirmation(registerRequest);

        ArgumentCaptor<UsersEntity> userCaptor = ArgumentCaptor.forClass(UsersEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UsersEntity passedToSave = userCaptor.getValue();
        assertThat(passedToSave.getUsername()).isEqualTo("alice");
        assertThat(passedToSave.getPassword()).isEqualTo("encodedPass");
        assertThat(passedToSave.isEnabled()).isFalse();
        assertThat(passedToSave.isAccount_locked()).isTrue();

        verify(confirmationRepository).save(any(Confirmation.class));

    }

    @Test
    void saveConfirmation_noExistingRole_createsRole() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        RolesEntity newRole = new RolesEntity();
        newRole.setName("USER");
        newRole.setDescription("Default UserRole");
        when(roleRepository.save(any(RolesEntity.class))).thenReturn(newRole);

        when(passwordEncoder.encode("plainPass")).thenReturn("encodedPass");

        UsersEntity savedUser = new UsersEntity();
        savedUser.setUsername("alice");
        savedUser.setEmail("alice@example.com");
        savedUser.setPassword("encodedPass");
        savedUser.setFirstName("Alicja");
        savedUser.setLastName("Nowak");
        savedUser.setEnabled(false);
        savedUser.setAccount_expired(false);
        savedUser.setAccount_locked(true);
        savedUser.setCredentials_expired(false);
        savedUser.setRoles(Collections.singleton(newRole));
        when(userRepository.save(any(UsersEntity.class))).thenReturn(savedUser);

        userService.saveConfirmation(registerRequest);

        verify(roleRepository).save(any(RolesEntity.class));
        verify(userRepository).save(any(UsersEntity.class));
        verify(confirmationRepository).save(any(Confirmation.class));
    }

    // ----------------------------
    // confirm tests
    // ----------------------------

    @Test
    void confirm_invalidToken_throwsRuntimeException() {
        when(confirmationRepository.findByConfirmationCode("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.confirm("bad"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void confirm_expiredToken_deletesUserAndConfirmationAndThrows() {
        UsersEntity user = new UsersEntity();
        Confirmation confirmation = mock(Confirmation.class);
        when(confirmation.getCreatedDate()).thenReturn(LocalDateTime.now().minusMinutes(61));
        when(confirmation.getUser()).thenReturn(user);
        when(confirmationRepository.findByConfirmationCode("expired")).thenReturn(Optional.of(confirmation));

        assertThatThrownBy(() -> userService.confirm("expired"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token expired");

        verify(userRepository).delete(user);
        verify(confirmationRepository).delete(confirmation);
    }

    @Test
    void confirm_validToken_enablesUserAndCreatesProfile() {
        UsersEntity user = new UsersEntity();
        user.setEnabled(false);
        user.setAccount_locked(true);
        Confirmation confirmation = mock(Confirmation.class);
        when(confirmation.getCreatedDate()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(confirmation.getUser()).thenReturn(user);
        when(confirmationRepository.findByConfirmationCode("good")).thenReturn(Optional.of(confirmation));

        userService.confirm("good");

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccount_locked()).isFalse();
        verify(userRepository).save(user);

    }

    // ----------------------------
    // savePasswordReset tests
    // ----------------------------

    @Test
    void savePasswordReset_userNotFound_throwsRuntimeException() {
        when(userRepository.findByEmail("no@user.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.savePasswordReset("no@user.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Podany mail nie istnieje");
        verify(userRepository).findByEmail("no@user.com");
    }

    @Test
    void savePasswordReset_existingConfirmationDeletedAndNewSaved() {
        UsersEntity user = new UsersEntity();
        user.setEmail("bob@example.com");
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));

        PasswordResetConfirmation existing = mock(PasswordResetConfirmation.class);
        when(passwordResetConfirmationRepository.findByUser(user)).thenReturn(Optional.of(existing));

        userService.savePasswordReset("bob@example.com");

        verify(passwordResetConfirmationRepository).delete(existing);

        ArgumentCaptor<PasswordResetConfirmation> captor = ArgumentCaptor.forClass(PasswordResetConfirmation.class);
        verify(passwordResetConfirmationRepository).save(captor.capture());
        PasswordResetConfirmation newConf = captor.getValue();
        assertThat(newConf.getUser()).isEqualTo(user);

        verify(emailService).PasswordResetMessage(eq("bob@example.com"), anyString());
    }

    @Test
    void savePasswordReset_noExistingConfirmation_savesNewAndSendsEmail() {
        UsersEntity user = new UsersEntity();
        user.setEmail("carol@example.com");
        when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(user));
        when(passwordResetConfirmationRepository.findByUser(user)).thenReturn(Optional.empty());


        userService.savePasswordReset("carol@example.com");

        ArgumentCaptor<PasswordResetConfirmation> captor = ArgumentCaptor.forClass(PasswordResetConfirmation.class);
        verify(passwordResetConfirmationRepository).save(captor.capture());
        PasswordResetConfirmation saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        verify(emailService).PasswordResetMessage(eq("carol@example.com"), anyString());
    }

    // ----------------------------
    // confirmPasswordReset tests
    // ----------------------------

    @Test
    void confirmPasswordReset_invalidToken_throwsRuntimeException() {

        when(passwordResetConfirmationRepository.findBytoken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.confirmPasswordReset("bad", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void confirmPasswordReset_expiredToken_deletesAndThrows() {
        UsersEntity user = new UsersEntity();

        PasswordResetConfirmation prc = mock(PasswordResetConfirmation.class);
        when(prc.getCreated()).thenReturn(LocalDateTime.now().minusMinutes(61));
        when(passwordResetConfirmationRepository.findBytoken("expired")).thenReturn(Optional.of(prc));

        assertThatThrownBy(() -> userService.confirmPasswordReset("expired", "newPass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token expired");
        verify(passwordResetConfirmationRepository).delete(prc);
    }

    @Test
    void confirmPasswordReset_validToken_updatesPasswordAndDeletesConfirmation() {
        UsersEntity user = new UsersEntity();
        PasswordResetConfirmation prc = mock(PasswordResetConfirmation.class);
        when(prc.getCreated()).thenReturn(LocalDateTime.now().minusMinutes(10));
        when(prc.getUser()).thenReturn(user);
        when(passwordResetConfirmationRepository.findBytoken("good")).thenReturn(Optional.of(prc));

        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");


        userService.confirmPasswordReset("good", "newPass");


        assertThat(user.getPassword()).isEqualTo("encodedNewPass");
        verify(userRepository).save(user);

        verify(passwordResetConfirmationRepository).delete(prc);
    }
}

