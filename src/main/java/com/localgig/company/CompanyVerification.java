package com.localgig.company;

import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_verifications", uniqueConstraints =
        @UniqueConstraint(name = "uk_company_verification_user", columnNames = "user_id"))
public class CompanyVerification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 160) private String companyName;
    @Column(length = 100) private String registrationNumber;
    @Column(length = 500) private String website;
    @Column(length = 500) private String evidenceUrl;
    @Column(nullable = false, length = 20) private String status = "PENDING";
    @Column(nullable = false) private LocalDateTime submittedAt = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by") private User reviewedBy;
    private LocalDateTime reviewedAt;
    @Column(length = 1000) private String reviewNote;
    protected CompanyVerification() { }
    public CompanyVerification(User user, CompanyVerificationRequest request) {
        this.user = user; this.companyName = request.companyName().trim();
        this.registrationNumber = request.registrationNumber(); this.website = request.website(); this.evidenceUrl = request.evidenceUrl();
    }
    public void update(CompanyVerificationRequest request) {
        companyName = request.companyName().trim(); registrationNumber = request.registrationNumber();
        website = request.website(); evidenceUrl = request.evidenceUrl(); status = "PENDING";
        submittedAt = LocalDateTime.now(); reviewedBy = null; reviewedAt = null; reviewNote = null;
    }
    public void review(User admin, String status, String note) {
        this.status = status; this.reviewedBy = admin; this.reviewedAt = LocalDateTime.now(); this.reviewNote = note;
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCompanyName() { return companyName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getWebsite() { return website; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public String getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public User getReviewedBy() { return reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getReviewNote() { return reviewNote; }
}
