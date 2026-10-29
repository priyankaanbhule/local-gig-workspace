package com.localgig.admin;

import com.localgig.application.ApplicationRepository;
import com.localgig.application.ApplicationResponse;
import com.localgig.company.CompanyVerification;
import com.localgig.company.CompanyVerificationRepository;
import com.localgig.company.CompanyVerificationResponse;
import com.localgig.company.CompanyVerificationReviewRequest;
import com.localgig.opportunity.Opportunity;
import com.localgig.opportunity.OpportunityRepository;
import com.localgig.opportunity.OpportunityResponse;
import com.localgig.report.*;
import com.localgig.security.RoleGuard;
import com.localgig.user.User;
import com.localgig.user.UserRepository;
import com.localgig.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Transactional
public class AdminController {
    private final UserRepository users;
    private final OpportunityRepository opportunities;
    private final ApplicationRepository applications;
    private final ReportRepository reports;
    private final CompanyVerificationRepository verifications;
    private final RoleGuard roleGuard;
    public AdminController(UserRepository users, OpportunityRepository opportunities,
                           ApplicationRepository applications, ReportRepository reports,
                           CompanyVerificationRepository verifications, RoleGuard roleGuard) {
        this.users = users; this.opportunities = opportunities; this.applications = applications;
        this.reports = reports; this.verifications = verifications;
        this.roleGuard = roleGuard;
    }
    @GetMapping("/users")
    public List<UserSummary> users(@AuthenticationPrincipal User admin) {
        roleGuard.require(admin, UserRole.ADMIN);
        return users.findAll().stream().map(u -> new UserSummary(u.getId(), u.getFullName(), u.getEmail(),
                u.getRole().name(), u.isActive(), u.getCreatedAt())).toList();
    }
    @PatchMapping("/users/{userId}/active")
    public UserSummary setUserActive(@AuthenticationPrincipal User admin, @PathVariable Long userId,
                                    @RequestParam boolean active) {
        roleGuard.require(admin, UserRole.ADMIN);
        User target = users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (target.getRole() == UserRole.ADMIN && !active)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin accounts cannot be blocked here");
        target.setActive(active);
        return new UserSummary(target.getId(), target.getFullName(), target.getEmail(), target.getRole().name(), target.isActive(), target.getCreatedAt());
    }
    @GetMapping("/listings")
    public List<OpportunityResponse> listings(@AuthenticationPrincipal User admin) {
        roleGuard.require(admin, UserRole.ADMIN);
        return opportunities.findAll().stream().sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(OpportunityResponse::from).toList();
    }
    @GetMapping("/applications")
    public List<ApplicationResponse> applications(@AuthenticationPrincipal User admin) {
        roleGuard.require(admin, UserRole.ADMIN);
        return applications.findAll().stream().sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(ApplicationResponse::from).toList();
    }
    @GetMapping("/reports")
    public List<ReportResponse> reports(@AuthenticationPrincipal User admin) {
        roleGuard.require(admin, UserRole.ADMIN);
        return reports.findAllByOrderByCreatedAtDesc().stream().map(ReportResponse::from).toList();
    }
    @PatchMapping("/reports/{reportId}")
    public ReportResponse resolveReport(@AuthenticationPrincipal User admin, @PathVariable Long reportId,
                                        @Valid @RequestBody ReportResolutionRequest request) {
        roleGuard.require(admin, UserRole.ADMIN);
        String status = request.status().trim().toUpperCase();
        if (!status.equals("RESOLVED") && !status.equals("DISMISSED"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be RESOLVED or DISMISSED");
        Report report = reports.findById(reportId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        if (status.equals("RESOLVED") && report.getTargetType().equals("LISTING")) {
            opportunities.findById(report.getTargetId()).ifPresent(Opportunity::close);
        }
        if (status.equals("RESOLVED") && report.getTargetType().equals("USER")) {
            users.findById(report.getTargetId()).filter(target -> target.getRole() != UserRole.ADMIN)
                    .ifPresent(target -> target.setActive(false));
        }
        report.resolve(admin, status, request.note() == null ? "" : request.note().trim());
        return ReportResponse.from(report);
    }
    @GetMapping("/stats")
    public AdminStats stats(@AuthenticationPrincipal User admin) {
        roleGuard.require(admin, UserRole.ADMIN);
        long active = opportunities.countByStatus("ACTIVE");
        return new AdminStats(users.count(), active, opportunities.count(), applications.count(), reports.count(), LocalDateTime.now());
    }
    @GetMapping("/verifications")
    public List<CompanyVerificationResponse> verifications(@AuthenticationPrincipal User admin) {
        roleGuard.require(admin, UserRole.ADMIN);
        return verifications.findAllByOrderBySubmittedAtDesc().stream().map(CompanyVerificationResponse::from).toList();
    }
    @PatchMapping("/verifications/{verificationId}")
    public CompanyVerificationResponse reviewVerification(@AuthenticationPrincipal User admin,
            @PathVariable Long verificationId, @Valid @RequestBody CompanyVerificationReviewRequest request) {
        roleGuard.require(admin, UserRole.ADMIN);
        String status = request.status().trim().toUpperCase();
        if (!status.equals("VERIFIED") && !status.equals("REJECTED"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be VERIFIED or REJECTED");
        CompanyVerification item = verifications.findById(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification request not found"));
        item.review(admin, status, request.note() == null ? "" : request.note().trim());
        return CompanyVerificationResponse.from(item);
    }
    public record UserSummary(Long id, String fullName, String email, String role, boolean active, LocalDateTime createdAt) { }
    public record AdminStats(long users, long activeListings, long totalListings, long applications, long reports, LocalDateTime generatedAt) { }
}
