package com.localgig.company;

import com.localgig.security.RoleGuard;
import com.localgig.user.User;
import com.localgig.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/companies")
@Transactional
public class CompanyProfileController {
    private final CompanyProfileRepository profiles;
    private final CompanyVerificationRepository verifications;
    private final RoleGuard roleGuard;

    public CompanyProfileController(CompanyProfileRepository profiles,
            CompanyVerificationRepository verifications, RoleGuard roleGuard) {
        this.profiles = profiles;
        this.verifications = verifications;
        this.roleGuard = roleGuard;
    }

    @GetMapping("/profile/me")
    public CompanyProfileResponse mine(@AuthenticationPrincipal User user) {
        requireEmployer(user);
        CompanyProfile profile = profiles.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Create your company profile first"));
        return response(profile);
    }

    @PutMapping("/profile/me")
    public CompanyProfileResponse save(@AuthenticationPrincipal User user,
            @Valid @RequestBody CompanyProfileRequest request) {
        requireEmployer(user);
        CompanyProfile profile = profiles.findByUserId(user.getId())
                .orElseGet(() -> new CompanyProfile(user, request));
        profile.update(request);
        return response(profiles.save(profile));
    }

    @GetMapping("/{employerId}/profile")
    @Transactional(readOnly = true)
    public CompanyProfileResponse publicProfile(@PathVariable Long employerId) {
        CompanyProfile profile = profiles.findByUserId(employerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company profile not found"));
        return response(profile);
    }

    private CompanyProfileResponse response(CompanyProfile profile) {
        boolean verified = verifications.findByUserId(profile.getUser().getId())
                .map(item -> "VERIFIED".equals(item.getStatus())).orElse(false);
        return CompanyProfileResponse.from(profile, verified);
    }

    private void requireEmployer(User user) {
        roleGuard.requireAny(user, UserRole.EMPLOYER, UserRole.ADMIN);
    }
}
