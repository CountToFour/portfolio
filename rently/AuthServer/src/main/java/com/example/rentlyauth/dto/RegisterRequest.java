package com.example.rentlyauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Dane rejestracyjne nowego użytkownika")
@Data
public class RegisterRequest {

    @Schema(description = "Nazwa użytkownika", example = "jan_kowalski", required = true)
    private String username;

    @Schema(description = "Hasło użytkownika", example = "P@ssw0rd!", required = true)
    private String password;

    @Schema(description = "Adres e-mail użytkownika", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "Imię użytkownika", example = "Jan", required = false)
    private String firstName;

    @Schema(description = "Nazwisko użytkownika", example = "Kowalski", required = false)
    private String lastName;
}
