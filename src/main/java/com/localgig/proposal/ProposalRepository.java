package com.localgig.proposal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    boolean existsByFreelancerIdAndOpportunityId(Long freelancerId, Long opportunityId);
    List<Proposal> findAllByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);
    List<Proposal> findAllByOpportunityIdOrderByCreatedAtDesc(Long opportunityId);
    Optional<Proposal> findByIdAndFreelancerId(Long id, Long freelancerId);
    boolean existsByOpportunityIdAndStatus(Long opportunityId, ProposalStatus status);
}
