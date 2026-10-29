package com.localgig.proposal;

import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.opportunity.Opportunity;
import com.localgig.opportunity.OpportunityRepository;
import com.localgig.project.Project;
import com.localgig.project.ProjectRepository;
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
public class ProposalController {
    private final ProposalRepository proposals;
    private final OpportunityRepository opportunities;
    private final ProjectRepository projects;
    private final NotificationRepository notifications;

    public ProposalController(ProposalRepository proposals, OpportunityRepository opportunities,
                              ProjectRepository projects, NotificationRepository notifications) {
        this.proposals = proposals;
        this.opportunities = opportunities;
        this.projects = projects;
        this.notifications = notifications;
    }

    @PostMapping("/opportunities/{opportunityId}/proposals")
    @ResponseStatus(HttpStatus.CREATED)
    public ProposalResponse submit(@AuthenticationPrincipal User user, @PathVariable Long opportunityId,
                                   @Valid @RequestBody ProposalRequest request) {
        requireFreelancer(user);
        Opportunity opportunity = opportunities.findByIdAndStatus(opportunityId, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gig is unavailable"));
        if (!"GIG".equalsIgnoreCase(opportunity.getKind())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposals can only be submitted to gigs");
        }
        if (opportunity.getDeadline() != null && opportunity.getDeadline().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "The proposal deadline has passed");
        }
        if (proposals.existsByFreelancerIdAndOpportunityId(user.getId(), opportunityId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already submitted a proposal for this gig");
        }
        Proposal saved = proposals.save(new Proposal(user, opportunity, request));
        notifications.save(new Notification(opportunity.getEmployer(), "New gig proposal",
                user.getFullName() + " submitted a proposal for " + opportunity.getTitle(), "PROPOSAL"));
        return ProposalResponse.from(saved);
    }

    @GetMapping("/proposals/me")
    @Transactional(readOnly = true)
    public List<ProposalResponse> mine(@AuthenticationPrincipal User user) {
        requireFreelancer(user);
        return proposals.findAllByFreelancerIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ProposalResponse::from).toList();
    }

    @GetMapping("/opportunities/{opportunityId}/proposals")
    @Transactional(readOnly = true)
    public List<ProposalResponse> forOpportunity(@AuthenticationPrincipal User user,
                                                 @PathVariable Long opportunityId) {
        Opportunity opportunity = opportunities.findById(opportunityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gig not found"));
        requireOwnerOrAdmin(user, opportunity);
        return proposals.findAllByOpportunityIdOrderByCreatedAtDesc(opportunityId).stream()
                .map(ProposalResponse::from).toList();
    }

    @PatchMapping("/proposals/{proposalId}/status")
    public ProposalResponse review(@AuthenticationPrincipal User user, @PathVariable Long proposalId,
                                   @Valid @RequestBody ProposalStatusRequest request) {
        Proposal proposal = proposals.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));
        requireOwnerOrAdmin(user, proposal.getOpportunity());
        ProposalStatus next = request.status();
        if (next == ProposalStatus.WITHDRAWN || next == ProposalStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This status cannot be set by the employer");
        }
        if (proposal.getStatus() == ProposalStatus.ACCEPTED || proposal.getStatus() == ProposalStatus.REJECTED
                || proposal.getStatus() == ProposalStatus.WITHDRAWN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This proposal has already been finalized");
        }
        if (next == ProposalStatus.ACCEPTED) {
            if (proposals.existsByOpportunityIdAndStatus(proposal.getOpportunity().getId(), ProposalStatus.ACCEPTED)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A proposal has already been accepted for this gig");
            }
            proposal.setStatus(ProposalStatus.ACCEPTED);
            projects.save(new Project(proposal));
            notifications.save(new Notification(proposal.getFreelancer(), "Gig proposal accepted",
                    "Your proposal for " + proposal.getOpportunity().getTitle() + " was accepted. A project workspace is ready.", "PROJECT"));
        } else {
            proposal.setStatus(next);
            if (next == ProposalStatus.REJECTED) {
                notifications.save(new Notification(proposal.getFreelancer(), "Gig proposal update",
                        "Your proposal for " + proposal.getOpportunity().getTitle() + " was not selected.", "PROPOSAL"));
            }
        }
        return ProposalResponse.from(proposal);
    }

    @DeleteMapping("/proposals/{proposalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal User user, @PathVariable Long proposalId) {
        Proposal proposal = proposals.findByIdAndFreelancerId(proposalId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));
        if (proposal.getStatus() != ProposalStatus.SUBMITTED && proposal.getStatus() != ProposalStatus.SHORTLISTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This proposal can no longer be withdrawn");
        }
        proposal.setStatus(ProposalStatus.WITHDRAWN);
    }

    private void requireFreelancer(User user) {
        if (user == null || user.getRole() != UserRole.FREELANCER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Freelancer access required");
        }
    }

    private void requireOwnerOrAdmin(User user, Opportunity opportunity) {
        if (user == null || (!opportunity.getEmployer().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage proposals for this gig");
        }
    }
}
