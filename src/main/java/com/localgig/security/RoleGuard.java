package com.localgig.security;

import com.localgig.user.User;
import com.localgig.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

/** Shared role checks for controller actions that require a specific account role. */
@Component
public class RoleGuard {
    public void require(User user, UserRole role) {
        requireAny(user, role);
    }

    public void requireAny(User user, UserRole... allowedRoles) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to continue");
        }
        if (Arrays.stream(allowedRoles).noneMatch(role -> role == user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account role cannot perform this action");
        }
    }
}
