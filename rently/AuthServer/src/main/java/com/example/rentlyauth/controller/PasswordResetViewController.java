package com.example.rentlyauth.controller;

import com.example.rentlyauth.repository.PasswordResetConfirmationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PasswordResetView", description = "Wyświetlanie formularza zmiany hasła na podstawie tokenu")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetViewController {

    private final PasswordResetConfirmationRepository passwordResetConfirmationRepository;

    @Operation(summary = "Pokaż formularz zmiany hasła", description = "Sprawdza token i zwraca formularz lub błąd")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Token ważny - przekierowanie do formularza", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Token nie znaleziony", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/ConfirmationPassword")
    public ResponseEntity<?> showResetForm(
            @Parameter(name = "token", in = ParameterIn.QUERY, description = "Token do zmiany hasła", required = true)
            @RequestParam("token") String token) {

        if (passwordResetConfirmationRepository.findBytoken(token).isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).body("Token do zmiany hasła: " + token);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token not found");
        }
    }
}