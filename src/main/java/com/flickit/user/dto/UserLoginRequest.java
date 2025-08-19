package com.flickit.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank
    private String phone;

    @NotBlank
    private String password;
}
