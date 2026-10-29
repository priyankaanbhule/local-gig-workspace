package com.localgig.application;

import com.localgig.opportunity.Opportunity;
import com.localgig.opportunity.OpportunityRepository;
import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.user.User;
import com.localgig.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@Transactional
public class ApplicationController {
    private final ApplicationRepository applications;
    private final OpportunityRepository opportunities;
    private final NotificationRepository notifications;

    public ApplicationController(ApplicationRepository applications, OpportunityRepository opportunities,
                                 NotificationRepository notifications) {
        this.applications = applications;
        this.opportunities = opportunities;
        this.notifications = notifications;
    }

    @PostMapping("/opportunities/{opportunityId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@AuthenticationPrincipal User user,
                                     @PathVariable Long opportunityId,
                                     @Valid @RequestBody ApplicationRequest request) {
        requireCandidate(user);
        Opportunity opportunity = opportunities.findByIdAndStatus(opportunityId, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity is unavailable"));
        if (opportunity.getDeadline() != null && opportunity.getDeadline().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "The application deadline has passed");
        }
        if (applications.existsByApplicantIdAndOpportunityId(user.getId(), opportunityId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already applied to this opportunity");
        }
        JobApplication application = applications.save(new JobApplication(user, opportunity, request));
        notifications.save(new Notification(opportunity.getEmployer(), "New application received",
                user.getFullName() + " applied for " + opportunity.getTitle(), "APPLICATION"));
        return ApplicationResponse.from(application);
    }

    @GetMapping("/applications/me")
    public List<ApplicationResponse> myApplications(@AuthenticationPrincipal User user) {
        return applications.findAllByApplicantIdOrderByCreatedAtDesc(user.getId())
                .stream().map(ApplicationResponse::from).toList();
    }

    @GetMapping("/opportunities/{opportunityId}/applications")
    public List<ApplicationResponse> applicants(@AuthenticationPrincipal User user,
                                                @PathVariable Long opportunityId) {
        Opportunity opportunity = opportunities.findById(opportunityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
        if (!opportunity.getEmployer().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view these applicants");
        }
        return applications.findAllForOpportunity(opportunityId).stream()
                .map(ApplicationResponse::from).toList();
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ApplicationResponse updateStatus(@AuthenticationPrincipal User user,
                                            @PathVariable Long applicationId,
                                            @Valid @RequestBody ApplicationStatusRequest request) {
        JobApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        if (!application.getOpportunity().getEmployer().getId().equals(user.getId())
                && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update this application");
        }
        if (request.status() == ApplicationStatus.WITHDRAWN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidates withdraw their own applications");
        }
        application.setStatus(request.status());
        notifications.save(new Notification(application.getApplicant(), "Application updated",
                "Your application for " + application.getOpportunity().getTitle() + " is "
                        + request.status().name().toLowerCase().replace('_', ' ') + ".", "APPLICATION"));
        return ApplicationResponse.from(application);
    }

    @DeleteMapping("/applications/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal User user, @PathVariable Long applicationId) {
        JobApplication application = applications.findByIdAndApplicantId(applicationId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        application.setStatus(ApplicationStatus.WITHDRAWN);
    }

    private void requireCandidate(User user) {
        if (user == null || (user.getRole() != UserRole.JOB_SEEKER && user.getRole() != UserRole.FREELANCER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Candidate access required");
        }
    }
}
