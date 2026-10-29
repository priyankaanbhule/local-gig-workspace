package com.localgig.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByApplicationId(Long applicationId);
    @Query("select p from PaymentRecord p where p.application.applicant.id = :userId or p.application.opportunity.employer.id = :userId order by p.createdAt desc")
    List<PaymentRecord> findForUser(@Param("userId") Long userId);
}
