package com.localgig.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByApplicantIdAndOpportunityId(Long applicantId, Long opportunityId);
    List<JobApplication> findAllByApplicantIdOrderByCreatedAtDesc(Long applicantId);
    Optional<JobApplication> findByIdAndApplicantId(Long id, Long applicantId);

    @Query("select a from JobApplication a where a.opportunity.id = :opportunityId order by a.createdAt desc")
    List<JobApplication> findAllForOpportunity(@Param("opportunityId") Long opportunityId);
    List<JobApplication> findAllByDeadlineReminderSentFalse();
}
