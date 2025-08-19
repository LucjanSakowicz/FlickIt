package com.flickit.user.dto;

import com.flickit.user.model.UserEntity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String name;
    private String phone;
    private Role role;
    private Double rating;
    private Integer ratingCount;
}
