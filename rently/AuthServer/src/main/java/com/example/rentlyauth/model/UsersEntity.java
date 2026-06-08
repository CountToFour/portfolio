package com.example.rentlyauth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UsersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean account_expired;
    private boolean account_locked;
    private boolean enabled;
    private boolean credentials_expired;
    @ManyToMany
    private Set<RolesEntity> roles;
}
