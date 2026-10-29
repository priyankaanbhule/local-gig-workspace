package com.localgig.company;

import java.time.LocalDateTime;

public record CompanyVerificationResponse(Long id, Long userId, String contactName, String email,
                                          String companyName, String registrationNumber, String website,
                                          String evidenceUrl, String status, LocalDateTime submittedAt,
                                          String reviewNote) {
    public static CompanyVerificationResponse from(CompanyVerification v) {
        return new CompanyVerificationResponse(v.getId(), v.getUser().getId(), v.getUser().getFullName(),
                v.getUser().getEmail(), v.getCompanyName(), v.getRegistrationNumber(), v.getWebsite(),
                v.getEvidenceUrl(), v.getStatus(), v.getSubmittedAt(), v.getReviewNote());
    }
}
