package com.flickit.user.controller;

import com.flickit.auth.model.CurrentUser;
import com.flickit.auth.service.AuthContext;
import com.flickit.user.dto.CreateUserRequest;
import com.flickit.user.dto.UserDto;
import com.flickit.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Registration and profile endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Register new user")
    public ResponseEntity<UserDto> register(@RequestBody @Valid CreateUserRequest req) {
        return ResponseEntity.ok(userService.create(req, req.getPassword()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by id (ADMIN)")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        UserDto dto = userService.getById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','VENDOR','ADMIN')")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> getSelf() {
        CurrentUser current = AuthContext.getCurrentUser();
        if (current == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(userService.getById(current.getId()));
    }

}
