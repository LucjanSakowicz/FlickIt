package com.flickit.auth.controller;

import com.flickit.auth.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication and token endpoints")
public class TokenController {

    private final JwtService jwtService;

    public TokenController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/test-token")
    @Operation(summary = "Generate a test JWT for given userId and role")
    public ResponseEntity<Map<String, String>> generateToken(
            @RequestParam @NotBlank UUID userId,
            @RequestParam @NotBlank String role
    ) {
        String token = jwtService.generateToken(userId, role.toUpperCase());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
