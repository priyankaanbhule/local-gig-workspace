package com.localgig.company;

import com.localgig.user.User;
import com.localgig.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/companies/verification")
@Transactional
public class CompanyVerificationController {
    private final CompanyVerificationRepository verifications;
    public CompanyVerificationController(CompanyVerificationRepository verifications) { this.verifications = verifications; }
    @GetMapping("/me")
    public CompanyVerificationResponse mine(@AuthenticationPrincipal User user) {
        requireEmployer(user);
        return verifications.findByUserId(user.getId()).map(CompanyVerificationResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No verification submitted"));
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyVerificationResponse submit(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody CompanyVerificationRequest request) {
        requireEmployer(user);
        CompanyVerification verification = verifications.findByUserId(user.getId()).orElse(null);
        if (verification == null) verification = new CompanyVerification(user, request);
        else if (verification.getStatus().equals("VERIFIED"))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This company is already verified");
        else verification.update(request);
        return CompanyVerificationResponse.from(verifications.save(verification));
    }
    private void requireEmployer(User user) {
        if (user == null || (user.getRole() != UserRole.EMPLOYER && user.getRole() != UserRole.ADMIN))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Employer access required");
    }
}
