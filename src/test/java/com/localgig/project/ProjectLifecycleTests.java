package com.localgig.project;

import com.localgig.opportunity.Opportunity;
import com.localgig.opportunity.OpportunityRequest;
import com.localgig.proposal.Proposal;
import com.localgig.proposal.ProposalRequest;
import com.localgig.proposal.ProposalStatus;
import com.localgig.user.User;
import com.localgig.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectLifecycleTests {

    @Test
    void acceptedProposalCanTrackProgressAndSubmitWork() {
        Project project = projectFixture();

        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());
        assertEquals(0, project.getProgressPercent());

        project.updateProgress(45, "Wireframes and first page are ready");
        assertEquals(ProjectStatus.IN_PROGRESS, project.getStatus());
        assertEquals(45, project.getProgressPercent());

        project.updateProgress(100, "All requested pages are complete");
        assertEquals(ProjectStatus.SUBMITTED, project.getStatus());
    }

    @Test
    void employerCanCompleteOnlySubmittedProjectAndCompletedProjectIsLocked() {
        Project project = projectFixture();

        assertThrows(IllegalStateException.class, project::complete);
        project.updateDeliverable("https://example.test/deliverable");
        project.complete();

        assertEquals(ProjectStatus.COMPLETED, project.getStatus());
        assertThrows(IllegalStateException.class, () -> project.updateProgress(100, "Change after completion"));
    }

    @Test
    void proposalStoresItsBidAndDeliveryEstimate() {
        Proposal proposal = proposalFixture();

        assertEquals(ProposalStatus.SUBMITTED, proposal.getStatus());
        assertEquals(new BigDecimal("750.00"), proposal.getBidAmount());
        assertEquals(7, proposal.getEstimatedDays());
        assertEquals("I can deliver the requested website.", proposal.getCoverLetter());
    }

    private Project projectFixture() {
        return new Project(proposalFixture());
    }

    private Proposal proposalFixture() {
        User employer = new User("Local Client", "client@example.test", "encoded", UserRole.EMPLOYER);
        User freelancer = new User("Local Freelancer", "freelancer@example.test", "encoded", UserRole.FREELANCER);
        Opportunity gig = new Opportunity(employer, new OpportunityRequest(
                "Small business website", "Local Client", "Remote", "Design", "GIG", "Freelance",
                "Negotiable", "Build a responsive business website", List.of(), null, null, null));
        ProposalRequest request = new ProposalRequest(
                "  I can deliver the requested website.  ", new BigDecimal("750.00"), 7);
        return new Proposal(freelancer, gig, request);
    }
}
