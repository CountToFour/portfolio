package com.example.rentlyauth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@EqualsAndHashCode(exclude = "user")
public class Confirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String confirmationCode;
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private LocalDateTime createdDate;
    @OneToOne(targetEntity = UsersEntity.class, fetch = FetchType.EAGER)
    private UsersEntity user;

    public Confirmation(UsersEntity user) {
        this.user = user;
        this.createdDate = LocalDateTime.now();
        this.confirmationCode = UUID.randomUUID().toString();
    }

}
