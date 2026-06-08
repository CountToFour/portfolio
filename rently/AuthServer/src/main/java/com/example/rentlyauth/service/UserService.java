package com.example.rentlyauth.service;


import com.example.rentlyauth.dto.RegisterRequest;
import com.example.rentlyauth.model.Confirmation;
import com.example.rentlyauth.model.PasswordResetConfirmation;
import com.example.rentlyauth.model.RolesEntity;
import com.example.rentlyauth.model.UsersEntity;
import com.example.rentlyauth.repository.ConfirmationRepository;
import com.example.rentlyauth.repository.PasswordResetConfirmationRepository;
import com.example.rentlyauth.repository.RoleRepository;
import com.example.rentlyauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ConfirmationRepository confirmationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetConfirmationRepository passwordResetConfirmationRepository;
    @Transactional
    public void saveConfirmation(RegisterRequest registerRequest) {
        RolesEntity userRole = roleRepository.findByName("USER").orElseGet(() -> {
            RolesEntity newRole = new RolesEntity();
            newRole.setName("USER");
            newRole.setDescription("Default UserRole");
            return roleRepository.save(newRole);
        });

        UsersEntity user = new UsersEntity();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEnabled(false);
        user.setAccount_expired(false);
        user.setAccount_locked(true);
        user.setCredentials_expired(false);
        user.setRoles(Collections.singleton(userRole));

        UsersEntity savedUser = userRepository.save(user);
        savedUser.setUsername(savedUser.getUsername()+'#'+savedUser.getId());



        Confirmation confirmation = new Confirmation(savedUser);
        confirmationRepository.save(confirmation);
        emailService.AccountVerificationMessage(user.getUsername(), user.getEmail(), confirmation.getConfirmationCode());
    }

    @Transactional
    public void confirm(String token) {
        Optional<Confirmation> confirmation=confirmationRepository.findByConfirmationCode(token);
        if(confirmation.isEmpty()) {
            throw new RuntimeException("Invalid token");
        }
        if (confirmation.get().getCreatedDate().isBefore(LocalDateTime.now().minusMinutes(60))) {
            userRepository.delete(confirmation.get().getUser());
            confirmationRepository.delete(confirmation.get());
            throw new RuntimeException("Token expired");
        }

        UsersEntity user=confirmation.get().getUser();
        user.setEnabled(true);
        user.setAccount_locked(false);
        userRepository.save(user);

        //return Boolean.TRUE;
    }
    public void savePasswordReset(String mail) {
        Optional<UsersEntity> user=userRepository.findByEmail(mail);
        if(user.isEmpty()) {
            throw new RuntimeException("Podany mail nie istnieje");
        }
        passwordResetConfirmationRepository.findByUser(user.get()).ifPresent(existingConfirmation -> {
            //można potem dodac resenda
            passwordResetConfirmationRepository.delete(existingConfirmation);
        });
       // user.get().setEnabled(false);
       // user.get().setAccount_locked(true);
        PasswordResetConfirmation passwordResetConfirmation = new PasswordResetConfirmation(user.get());
        passwordResetConfirmationRepository.save(passwordResetConfirmation);
        emailService.PasswordResetMessage(user.get().getEmail(), passwordResetConfirmation.getToken());

    }
    @Transactional
    public void confirmPasswordReset(String token,String password) {
        Optional<PasswordResetConfirmation> passwordResetConfirmation=passwordResetConfirmationRepository.findBytoken(token);
        if(passwordResetConfirmation.isEmpty()) {
            throw new RuntimeException("Invalid token");
        }
        if(passwordResetConfirmation.get().getCreated().isBefore(LocalDateTime.now().minusMinutes(60))) {
            passwordResetConfirmationRepository.delete(passwordResetConfirmation.get());
            throw new RuntimeException("Token expired");
        }
        UsersEntity user=passwordResetConfirmation.get().getUser();
       // user.setEnabled(true);
        //user.setAccount_locked(false);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        passwordResetConfirmationRepository.delete(passwordResetConfirmation.get());
    }
}
