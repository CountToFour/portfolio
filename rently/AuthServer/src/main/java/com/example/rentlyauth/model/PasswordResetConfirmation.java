package com.example.rentlyauth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(exclude = "user")
public class PasswordResetConfirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String token;
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private LocalDateTime created;
    @OneToOne(targetEntity = UsersEntity.class,fetch = FetchType.EAGER)
    private UsersEntity user;
    public PasswordResetConfirmation(UsersEntity user) {
        this.user = user;
        this.created = LocalDateTime.now();
        this.token = UUID.randomUUID().toString();
    }
}
