package com.localgig.profile;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProfileRequest(
        @Size(max = 120) String headline,
        @Size(max = 2000) String summary,
        @Size(max = 30) String phone,
        @Size(max = 100) String city,
        @Size(max = 100) String country,
        List<String> skills,
        List<String> education,
        List<String> experience,
        List<String> projects,
        List<String> certifications,
        List<String> preferredRoles,
        List<String> preferredLocations,
        @PositiveOrZero BigDecimal expectedSalary,
        @Size(max = 500) String portfolioUrl,
        @Size(max = 500) String resumeUrl,
        @Size(max = 500) String profilePictureUrl,
        Boolean available
) {
}
