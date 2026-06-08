package com.example.rentlyauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Żądanie wysłania e-maila do zmiany hasła")
@Data
public class PasswordChangeRequest {

    @Schema(description = "Adres e-mail użytkownika", example = "user@example.com", required = true)
    private String email;
}