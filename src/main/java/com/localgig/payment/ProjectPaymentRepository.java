package com.localgig.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectPaymentRepository extends JpaRepository<ProjectPaymentRecord, Long> {
    Optional<ProjectPaymentRecord> findByProjectId(Long projectId);

    @Query("select p from ProjectPaymentRecord p where p.project.freelancer.id = :userId or p.project.employer.id = :userId order by p.createdAt desc")
    List<ProjectPaymentRecord> findForUser(@Param("userId") Long userId);
}
