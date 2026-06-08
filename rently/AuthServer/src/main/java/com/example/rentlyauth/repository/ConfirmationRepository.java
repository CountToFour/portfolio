package com.example.rentlyauth.repository;

import com.example.rentlyauth.model.Confirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation,Long> {
Optional<Confirmation> findByConfirmationCode(String confirmationCode);
}
