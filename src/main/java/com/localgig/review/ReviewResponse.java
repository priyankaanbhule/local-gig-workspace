package com.localgig.review;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long applicationId, Long projectId, Long reviewerId, String reviewerName,
                             Long revieweeId, String revieweeName, int rating, String body,
                             LocalDateTime createdAt) {
    public static ReviewResponse from(Review r) {
        return new ReviewResponse(r.getId(), r.getApplication().getId(), null, r.getReviewer().getId(),
                r.getReviewer().getFullName(), r.getReviewee().getId(), r.getReviewee().getFullName(),
                r.getRating(), r.getBody(), r.getCreatedAt());
    }

    public static ReviewResponse from(ProjectReview r) {
        return new ReviewResponse(r.getId(), null, r.getProject().getId(), r.getReviewer().getId(),
                r.getReviewer().getFullName(), r.getReviewee().getId(), r.getReviewee().getFullName(),
                r.getRating(), r.getBody(), r.getCreatedAt());
    }
}
