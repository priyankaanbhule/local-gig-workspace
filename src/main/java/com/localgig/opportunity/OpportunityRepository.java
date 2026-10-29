package com.localgig.opportunity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    List<Opportunity> findAllByStatusOrderByCreatedAtDesc(String status);
    List<Opportunity> findAllByEmployerIdOrderByCreatedAtDesc(Long employerId);
    Optional<Opportunity> findByIdAndStatus(Long id, String status);
    long countByStatus(String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Opportunity o set o.status = 'EXPIRED' " +
            "where o.status = 'ACTIVE' and o.deadline is not null and o.deadline < :today")
    int expirePastDue(@Param("today") LocalDate today);
}
