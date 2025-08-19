package com.flickit.auth.controller;

import com.flickit.auth.service.JwtService;
import com.flickit.user.dto.UserLoginRequest;
import com.flickit.user.repository.UserRepository;
import com.flickit.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and token endpoints")
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private static final Long EXPIRATION_MS = 3600*1000L;

    @PostMapping("/token")
    @Operation(summary = "Generate a short-lived token (admin/test)")
    public ResponseEntity<String> generateToken(@RequestParam UUID userId, @RequestParam String role) {
        String token = jwtService.generateToken(userId, role, EXPIRATION_MS);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and return JWT token")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid UserLoginRequest req) {
        UUID userId = userService.login(req.getPhone(), req.getPassword());
        String role = userRepository.findById(userId).orElseThrow().getRole().name();
        String token = jwtService.generateToken(userId, role, 7 * 24 * 3600000L);

        return ResponseEntity.ok(Map.of("token", token));
    }

}
