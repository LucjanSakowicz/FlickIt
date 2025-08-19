package com.flickit.auth.service;

import com.flickit.auth.model.CurrentUser;
import com.flickit.user.model.UserEntity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class AuthContext {

    public static CurrentUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UUID)) return null;

        UUID userId = (UUID) auth.getPrincipal();
        String roleStr = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("CUSTOMER");

        return new CurrentUser(userId, Role.valueOf(roleStr));
    }
}
