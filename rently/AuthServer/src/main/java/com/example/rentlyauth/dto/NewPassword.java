package com.example.rentlyauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Payload do ustawienia nowego hasła")
@Data
public class NewPassword {

    @Schema(description = "Nowe hasło użytkownika", example = "P@ssw0rd!", required = true)
    private String password;

    @Schema(description = "Token do resetu hasła", example = "abc123-token", required = true)
    private String token;
}
