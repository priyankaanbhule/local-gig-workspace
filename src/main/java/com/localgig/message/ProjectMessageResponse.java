package com.localgig.message;

import java.time.LocalDateTime;

public record ProjectMessageResponse(Long id, Long projectId, Long senderId, String senderName,
                                     String body, boolean read, LocalDateTime createdAt) {
    public static ProjectMessageResponse from(ProjectMessage message) {
        return new ProjectMessageResponse(message.getId(), message.getProject().getId(),
                message.getSender().getId(), message.getSender().getFullName(), message.getBody(),
                message.isRead(), message.getCreatedAt());
    }
}
