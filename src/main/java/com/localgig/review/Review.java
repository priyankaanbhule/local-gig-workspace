package com.localgig.review;

import com.localgig.application.JobApplication;
import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints =
        @UniqueConstraint(name = "uk_review_application_reviewer", columnNames = {"application_id", "reviewer_id"}))
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;
    @Column(nullable = false) private int rating;
    @Column(nullable = false, length = 2000) private String body;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    protected Review() { }
    public Review(JobApplication application, User reviewer, User reviewee, int rating, String body) {
        this.application = application; this.reviewer = reviewer; this.reviewee = reviewee;
        this.rating = rating; this.body = body;
    }
    public Long getId() { return id; }
    public JobApplication getApplication() { return application; }
    public User getReviewer() { return reviewer; }
    public User getReviewee() { return reviewee; }
    public int getRating() { return rating; }
    public String getBody() { return body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
