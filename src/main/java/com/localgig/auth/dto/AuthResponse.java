package com.localgig.auth.dto;

import com.localgig.user.User;
import com.localgig.user.UserRole;

public record AuthResponse(
        Long id,
        String fullName,
        String email,
        UserRole role
) {
    public static AuthResponse from(User user) {
        return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}