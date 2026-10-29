package com.localgig.profile;

import com.localgig.user.User;

import java.math.BigDecimal;
import java.util.List;

public record ProfileResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        String headline,
        String summary,
        String phone,
        String city,
        String country,
        List<String> skills,
        List<String> education,
        List<String> experience,
        List<String> projects,
        List<String> certifications,
        List<String> preferredRoles,
        List<String> preferredLocations,
        BigDecimal expectedSalary,
        String portfolioUrl,
        String resumeUrl,
        String profilePictureUrl,
        boolean available
) {
    public static ProfileResponse emptyFor(User user) {
        return new ProfileResponse(
                null,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                true
        );
    }

    public static ProfileResponse from(Profile profile) {
        User user = profile.getUser();

        return new ProfileResponse(
                profile.getId(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                profile.getHeadline(),
                profile.getSummary(),
                profile.getPhone(),
                profile.getCity(),
                profile.getCountry(),
                List.copyOf(profile.getSkills()),
                List.copyOf(profile.getEducation()),
                List.copyOf(profile.getExperience()),
                List.copyOf(profile.getProjects()),
                List.copyOf(profile.getCertifications()),
                List.copyOf(profile.getPreferredRoles()),
                List.copyOf(profile.getPreferredLocations()),
                profile.getExpectedSalary(),
                profile.getPortfolioUrl(),
                profile.getResumeUrl(),
                profile.getProfilePictureUrl(),
                profile.isAvailable()
        );
    }
}
