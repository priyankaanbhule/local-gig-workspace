package com.localgig.company;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification, Long> {
    Optional<CompanyVerification> findByUserId(Long userId);
    List<CompanyVerification> findAllByOrderBySubmittedAtDesc();
}
