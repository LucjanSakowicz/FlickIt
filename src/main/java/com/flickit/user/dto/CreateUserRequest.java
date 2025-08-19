package com.flickit.user.dto;

import com.flickit.user.model.UserEntity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotBlank
    private String password;

    @NotNull
    private Role role;
}
