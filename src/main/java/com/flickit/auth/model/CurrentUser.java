package com.flickit.auth.model;

import com.flickit.user.model.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUser {
    private UUID id;
    private UserEntity.Role role;
}
