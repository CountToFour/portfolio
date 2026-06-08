package com.example.rentlyauth.controller;
import com.example.rentlyauth.dto.NewPassword;
import com.example.rentlyauth.dto.PasswordChangeRequest;
import com.example.rentlyauth.dto.RegisterRequest;
import com.example.rentlyauth.repository.UserRepository;
import com.example.rentlyauth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Operacje związane z rejestracją i autoryzacją użytkowników")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final UserService userService;

    @Operation(summary = "Rejestracja użytkownika", description = "Rejestruje użytkownika i wysyła e-mail z linkiem potwierdzającym")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utworzono konto użytkownika"),
            @ApiResponse(responseCode = "400", description = "Email lub nazwa użytkownika już istnieje", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody(description = "Dane rejestracyjne użytkownika", required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody RegisterRequest registerRequest) {

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username already exists", HttpStatus.BAD_REQUEST);
        }
        userService.saveConfirmation(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Potwierdzenie konta", description = "Potwierdza rejestrację użytkownika na podstawie tokenu z linku e-mail")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Przekierowano po potwierdzeniu lub błędnym tokenie", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/ConfirmationEmail")
    public ResponseEntity<?> confirmUserAccount(
            @Parameter(name = "token", in = ParameterIn.QUERY, description = "Token potwierdzający rejestrację", required = true)
            @RequestParam("token") String confirmationToken) {

        try {
            userService.confirm(confirmationToken);
            return ResponseEntity.status(HttpStatus.FOUND).body("Registration confirmed successfully");
        } catch (RuntimeException ex) {
            String msg = ex.getMessage();
            if ("Token expired".equals(msg)) {
                return ResponseEntity.status(HttpStatus.FOUND).body("Token expired");
            }
            return ResponseEntity.status(HttpStatus.FOUND).body("Invalid registry token");
        }
    }

    @Operation(summary = "Zainicjuj zmianę hasła", description = "Wysyła e-mail z linkiem do zmiany hasła")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Wysłano e-mail z instrukcją zmiany hasła")
    })
    @PostMapping("/ChangePasswordEmail")
    public ResponseEntity<?> changePasswordEmail(
            @RequestBody(description = "Adres e-mail użytkownika do zmiany hasła", required = true,
                    content = @Content(schema = @Schema(implementation = PasswordChangeRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody PasswordChangeRequest passwordChangeRequest) {

        userService.savePasswordReset(passwordChangeRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Zatwierdź nowe hasło", description = "Ustawia nowe hasło użytkownika na podstawie tokenu")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hasło zostało zmienione pomyślnie", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/NewPassword")
    public ResponseEntity<?> changePassword(
            @RequestBody(description = "Token i nowe hasło", required = true,
                    content = @Content(schema = @Schema(implementation = NewPassword.class)))
            @org.springframework.web.bind.annotation.RequestBody NewPassword newPassword) {

        userService.confirmPasswordReset(newPassword.getToken(), newPassword.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body("Hasło zostało zmienione pomyślnie");
    }
}

