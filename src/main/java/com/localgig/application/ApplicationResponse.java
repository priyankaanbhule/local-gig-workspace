package com.localgig.application;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id, Long opportunityId, String opportunityTitle, String company,
        Long applicantId, String applicantName, String applicantEmail,
        String coverLetter, String resumeUrl, ApplicationStatus status, LocalDateTime createdAt
) {
    public static ApplicationResponse from(JobApplication value) {
        return new ApplicationResponse(value.getId(), value.getOpportunity().getId(),
                value.getOpportunity().getTitle(), value.getOpportunity().getCompany(),
                value.getApplicant().getId(), value.getApplicant().getFullName(),
                value.getApplicant().getEmail(), value.getCoverLetter(), value.getResumeUrl(),
                value.getStatus(), value.getCreatedAt());
    }
}
