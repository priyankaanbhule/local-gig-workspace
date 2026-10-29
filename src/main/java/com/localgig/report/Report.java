package com.localgig.report;

import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_reports")
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    @Column(nullable = false, length = 20) private String targetType;
    @Column(nullable = false) private Long targetId;
    @Column(nullable = false, length = 120) private String reason;
    @Column(length = 1500) private String details;
    @Column(nullable = false, length = 20) private String status = "OPEN";
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by") private User reviewedBy;
    @Column(length = 1000) private String resolutionNote;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;
    protected Report() { }
    public Report(User reporter, String targetType, Long targetId, String reason, String details) {
        this.reporter = reporter; this.targetType = targetType; this.targetId = targetId;
        this.reason = reason; this.details = details;
    }
    public void resolve(User admin, String status, String note) {
        this.reviewedBy = admin; this.status = status; this.resolutionNote = note; this.resolvedAt = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public User getReporter() { return reporter; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getReason() { return reason; }
    public String getDetails() { return details; }
    public String getStatus() { return status; }
    public User getReviewedBy() { return reviewedBy; }
    public String getResolutionNote() { return resolutionNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
