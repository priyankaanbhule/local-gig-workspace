package com.localgig.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyFollowRepository extends JpaRepository<CompanyFollow, Long> {
    boolean existsByFollower_IdAndCompany_Id(Long followerId, Long companyId);
    Optional<CompanyFollow> findByFollower_IdAndCompany_Id(Long followerId, Long companyId);
    List<CompanyFollow> findAllByFollower_IdOrderByCreatedAtDesc(Long followerId);
}
