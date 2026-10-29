package com.localgig.review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByApplicationIdAndReviewerId(Long applicationId, Long reviewerId);
    List<Review> findAllByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);
}
