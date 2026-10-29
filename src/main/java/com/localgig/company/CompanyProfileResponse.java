package com.localgig.company;

import java.time.LocalDateTime;

public record CompanyProfileResponse(Long id, Long employerId, String employerName,
        String companyName, String tagline, String description, String website, String linkedinUrl,
        String industry, String headquarters, String logoUrl, String employeeCount,
        boolean verified, LocalDateTime updatedAt) {
    public static CompanyProfileResponse from(CompanyProfile profile, boolean verified) {
        return new CompanyProfileResponse(profile.getId(), profile.getUser().getId(),
                profile.getUser().getFullName(), profile.getCompanyName(), profile.getTagline(),
                profile.getDescription(), profile.getWebsite(), profile.getLinkedinUrl(), profile.getIndustry(),
                profile.getHeadquarters(), profile.getLogoUrl(), profile.getEmployeeCount(),
                verified, profile.getUpdatedAt());
    }
}
