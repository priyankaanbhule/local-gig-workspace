package com.localgig.auth;

import com.localgig.auth.dto.AuthResponse;
import com.localgig.auth.dto.LoginRequest;
import com.localgig.auth.dto.RegisterRequest;
import com.localgig.auth.dto.ChangePasswordRequest;
import com.localgig.auth.dto.ForgotPasswordRequest;
import com.localgig.auth.dto.ResetPasswordRequest;
import com.localgig.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.localgig.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AccountTokenRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService passwordResetEmailService;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            AccountTokenRepository tokens,
            UserRepository users,
            PasswordEncoder passwordEncoder,
            PasswordResetEmailService passwordResetEmailService
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.tokens = tokens;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetEmailService = passwordResetEmailService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.from(user));
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.email().trim(),
                                    request.password()
                            )
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(
                    context, httpRequest, httpResponse
            );

            return AuthResponse.from((User) authentication.getPrincipal());
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Email or password is incorrect"
            );
        }
    }

    @GetMapping("/me")
    public AuthResponse currentUser(
            @AuthenticationPrincipal User user
    ) {
        return AuthResponse.from(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        new SecurityContextLogoutHandler()
                .logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        users.findByEmailIgnoreCase(request.email().trim()).ifPresent(user -> {
            tokens.deleteAllByUserIdAndType(user.getId(), "RESET_PASSWORD");
            AccountToken token = tokens.save(new AccountToken(user, "RESET_PASSWORD", 30));
            passwordResetEmailService.sendResetLink(user.getEmail(), token.getToken());
        });
        return ResponseEntity.accepted().contentType(MediaType.TEXT_PLAIN)
                .body("If the account exists, password reset instructions will be sent.");
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        AccountToken token = tokens.findByTokenAndTypeAndExpiresAtAfter(AccountToken.hash(request.token()), "RESET_PASSWORD", java.time.LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token is invalid or expired"));
        token.getUser().changePassword(passwordEncoder.encode(request.newPassword()));
        tokens.delete(token);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("Password updated. Sign in with your new password.");
    }

    @PostMapping("/change-password")
    @Transactional
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("Password changed.");
    }

    @DeleteMapping("/account")
    @Transactional
    public ResponseEntity<Void> deactivateAccount(@AuthenticationPrincipal User user,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  Authentication authentication) {
        user.setActive(false);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }
}
