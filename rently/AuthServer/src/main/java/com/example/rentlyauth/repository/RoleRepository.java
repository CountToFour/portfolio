package com.example.rentlyauth.repository;


import com.example.rentlyauth.model.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RolesEntity, Long> {
    Optional<RolesEntity> findByName(String name);
}
