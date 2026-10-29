package com.localgig.alert;

import java.time.LocalDateTime;
import java.util.List;

public record JobAlertResponse(Long id, String keywords, String location, List<String> skills,
                               boolean active, LocalDateTime createdAt) {
    public static JobAlertResponse from(JobAlert alert) {
        return new JobAlertResponse(alert.getId(), alert.getKeywords(), alert.getLocation(),
                alert.getSkills(), alert.isActive(), alert.getCreatedAt());
    }
}
