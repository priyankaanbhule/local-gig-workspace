package com.localgig.company;

import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_profiles", uniqueConstraints =
        @UniqueConstraint(name = "uk_company_profile_user", columnNames = "user_id"))
public class CompanyProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 160) private String companyName;
    @Column(length = 180) private String tagline;
    @Column(length = 3000) private String description;
    @Column(length = 500) private String website;
    @Column(length = 500) private String linkedinUrl;
    @Column(length = 120) private String industry;
    @Column(length = 120) private String headquarters;
    @Column(length = 500) private String logoUrl;
    @Column(length = 50) private String employeeCount;
    @Column(nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();

    protected CompanyProfile() { }
    public CompanyProfile(User user, CompanyProfileRequest request) { this.user = user; update(request); }
    public void update(CompanyProfileRequest request) {
        companyName = request.companyName().trim(); tagline = clean(request.tagline());
        description = clean(request.description()); website = clean(request.website());
        linkedinUrl = clean(request.linkedinUrl());
        industry = clean(request.industry()); headquarters = clean(request.headquarters());
        logoUrl = clean(request.logoUrl()); employeeCount = clean(request.employeeCount());
        updatedAt = LocalDateTime.now();
    }
    private String clean(String value) { return value == null ? "" : value.trim(); }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCompanyName() { return companyName; }
    public String getTagline() { return tagline; }
    public String getDescription() { return description; }
    public String getWebsite() { return website; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public String getIndustry() { return industry; }
    public String getHeadquarters() { return headquarters; }
    public String getLogoUrl() { return logoUrl; }
    public String getEmployeeCount() { return employeeCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
