package com.localgig.project;

import com.localgig.proposal.Proposal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(Long id, Long proposalId, Long opportunityId, String title, String company, String opportunityKind,
                              String freelancerName, String employerName, BigDecimal agreedBudget,
                              ProjectStatus status, int progressPercent, String progressNote,
                              String deliverableUrl, LocalDate dueDate, LocalDateTime createdAt,
                              LocalDateTime completedAt) {
    public static ProjectResponse from(Project project) {
        Proposal proposal = project.getProposal();
        return new ProjectResponse(project.getId(), proposal.getId(), proposal.getOpportunity().getId(),
                proposal.getOpportunity().getTitle(), proposal.getOpportunity().getCompany(), proposal.getOpportunity().getKind(),
                project.getFreelancer().getFullName(), project.getEmployer().getFullName(),
                proposal.getBidAmount(), project.getStatus(), project.getProgressPercent(),
                project.getProgressNote(), project.getDeliverableUrl(), project.getDueDate(),
                project.getCreatedAt(), project.getCompletedAt());
    }
}
