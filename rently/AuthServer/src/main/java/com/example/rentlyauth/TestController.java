package com.example.rentlyauth;

import com.example.rentlyauth.dto.ReturnUserDTO;
import com.example.rentlyauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class TestController {
    @GetMapping
    public String sayHello() {
        return "Udało ci sie zalogować przez sesje Spring Boot! Raczej nie przez JWT!";
    }
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/user-id")
    public ResponseEntity<?> getCurrentUserId(Principal principal) {
        String mail = principal.getName();
        return userRepository.findByEmail(mail)
                .map(user -> ResponseEntity.ok(Map.of("userId", user.getId())))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }
    @GetMapping("/tokenTest")
    public ResponseEntity<?> tokenTest(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            response.put("token",jwt.getClaims());
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/findUserByID/{userId}")
    public ResponseEntity<ReturnUserDTO> getUserById(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(ReturnUserDTO::fromEntity)          
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
