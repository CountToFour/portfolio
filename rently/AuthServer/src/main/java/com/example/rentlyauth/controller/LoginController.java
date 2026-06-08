package com.example.rentlyauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Login", description = "Endpoint do wyświetlania strony logowania")
@Controller
public class LoginController {

    @Operation(summary = "Strona logowania użytkownika", description = "Zwraca widok strony logowania")
    @GetMapping("/login")
    public String login() {
        return "custom-login";
    }
}


