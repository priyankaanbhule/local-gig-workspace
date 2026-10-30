package com.localgig.message;

import java.time.LocalDateTime;

public record DirectMessageResponse(Long id, Long senderId, String senderName,
                                    Long recipientId, String recipientName,
                                    String body, boolean read, LocalDateTime createdAt) {
    public static DirectMessageResponse from(DirectMessage message) {
        return new DirectMessageResponse(message.getId(), message.getSender().getId(),
                message.getSender().getFullName(), message.getRecipient().getId(),
                message.getRecipient().getFullName(), message.getBody(), message.isRead(),
                message.getCreatedAt());
    }
}
