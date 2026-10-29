package com.localgig.message;

import java.time.LocalDateTime;

public record MessageResponse(Long id, Long applicationId, Long senderId, String senderName,
                              String body, boolean read, LocalDateTime createdAt) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(message.getId(), message.getApplication().getId(),
                message.getSender().getId(), message.getSender().getFullName(), message.getBody(),
                message.isRead(), message.getCreatedAt());
    }
}
