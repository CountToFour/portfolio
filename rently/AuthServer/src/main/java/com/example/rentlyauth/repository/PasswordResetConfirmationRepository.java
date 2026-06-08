package com.example.rentlyauth.repository;


import com.example.rentlyauth.model.PasswordResetConfirmation;
import com.example.rentlyauth.model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetConfirmationRepository extends JpaRepository<PasswordResetConfirmation, Long> {
    Optional<PasswordResetConfirmation> findBytoken(String token);
    Optional<PasswordResetConfirmation> findByUser(UsersEntity user);
}
