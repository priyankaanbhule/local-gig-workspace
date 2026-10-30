package com.localgig.auth;

import com.localgig.auth.dto.RegisterRequest;
import com.localgig.user.User;
import com.localgig.user.UserRepository;
import com.localgig.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (request.role() == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin accounts cannot be created through registration"
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }

        UserRole role = request.role() == UserRole.FREELANCER ? UserRole.JOB_SEEKER : request.role();
        User user = new User(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                role
        );

        // Email verification is not part of this prototype's sign-up flow.
        user.markEmailVerified();
        return userRepository.save(user);
    }
}
