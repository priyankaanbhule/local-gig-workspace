package com.localgig.proposal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProposalResponse(Long id, Long opportunityId, String opportunityTitle, String company,
                               Long freelancerId, String freelancerName, String freelancerEmail,
                               String coverLetter, BigDecimal bidAmount, Integer estimatedDays,
                               ProposalStatus status, LocalDateTime createdAt) {
    public static ProposalResponse from(Proposal proposal) {
        return new ProposalResponse(proposal.getId(), proposal.getOpportunity().getId(),
                proposal.getOpportunity().getTitle(), proposal.getOpportunity().getCompany(),
                proposal.getFreelancer().getId(), proposal.getFreelancer().getFullName(),
                proposal.getFreelancer().getEmail(), proposal.getCoverLetter(), proposal.getBidAmount(),
                proposal.getEstimatedDays(), proposal.getStatus(), proposal.getCreatedAt());
    }
}
