package com.localgig.review;

import com.localgig.project.Project;
import com.localgig.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_reviews", uniqueConstraints =
        @UniqueConstraint(name = "uk_project_review_reviewer", columnNames = {"project_id", "reviewer_id"}))
public class ProjectReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ProjectReview() { }

    public ProjectReview(Project project, User reviewer, User reviewee, int rating, String body) {
        this.project = project;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.rating = rating;
        this.body = body;
    }

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public User getReviewer() { return reviewer; }
    public User getReviewee() { return reviewee; }
    public int getRating() { return rating; }
    public String getBody() { return body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
