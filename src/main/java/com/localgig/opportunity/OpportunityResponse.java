package com.localgig.opportunity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OpportunityResponse(
        Long id, String title, String company, String location, String category,
        String kind, String type, String budget, String description, Double latitude, Double longitude,
        List<String> skills, LocalDate deadline, String status,
        LocalDateTime createdAt, Long employerId, Integer requiredExperienceYears
) {
    public static OpportunityResponse from(Opportunity value) {
        return new OpportunityResponse(value.getId(), value.getTitle(), value.getCompany(),
                value.getLocation(), value.getCategory(), value.getKind(), value.getType(),
                value.getBudget(), value.getDescription(), value.getLatitude(), value.getLongitude(), value.getSkills(), value.getDeadline(),
                value.getStatus(), value.getCreatedAt(), value.getEmployer().getId(), value.getRequiredExperienceYears());
    }
}
