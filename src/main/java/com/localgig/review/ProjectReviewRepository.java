package com.localgig.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectReviewRepository extends JpaRepository<ProjectReview, Long> {
    boolean existsByProjectIdAndReviewerId(Long projectId, Long reviewerId);
    List<ProjectReview> findAllByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);
    List<ProjectReview> findAllByReviewerIdOrderByCreatedAtDesc(Long reviewerId);
}
