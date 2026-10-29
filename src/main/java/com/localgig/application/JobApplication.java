package com.localgig.application;

import com.localgig.opportunity.Opportunity;
import com.localgig.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", uniqueConstraints =
        @UniqueConstraint(name = "uk_application_user_opportunity", columnNames = {"applicant_id", "opportunity_id"}))
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(length = 3000)
    private String coverLetter;

    @Column(length = 500)
    private String resumeUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deadlineReminderSent;

    protected JobApplication() { }

    public JobApplication(User applicant, Opportunity opportunity, ApplicationRequest request) {
        this.applicant = applicant;
        this.opportunity = opportunity;
        this.coverLetter = request.coverLetter() == null ? "" : request.coverLetter().trim();
        this.resumeUrl = request.resumeUrl() == null ? "" : request.resumeUrl().trim();
    }

    public void setStatus(ApplicationStatus status) { this.status = status; }
    public Long getId() { return id; }
    public User getApplicant() { return applicant; }
    public Opportunity getOpportunity() { return opportunity; }
    public String getCoverLetter() { return coverLetter; }
    public String getResumeUrl() { return resumeUrl; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isDeadlineReminderSent() { return deadlineReminderSent; }
    public void markDeadlineReminderSent() { deadlineReminderSent = true; }
}
