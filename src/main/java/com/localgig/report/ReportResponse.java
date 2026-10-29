package com.localgig.report;

import java.time.LocalDateTime;

public record ReportResponse(Long id, Long reporterId, String reporterName, String targetType,
                             Long targetId, String reason, String details, String status,
                             String resolutionNote, LocalDateTime createdAt, LocalDateTime resolvedAt) {
    public static ReportResponse from(Report r) {
        return new ReportResponse(r.getId(), r.getReporter().getId(), r.getReporter().getFullName(),
                r.getTargetType(), r.getTargetId(), r.getReason(), r.getDetails(), r.getStatus(),
                r.getResolutionNote(), r.getCreatedAt(), r.getResolvedAt());
    }
}
