package com.localgig.notification;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String title, String body, String type, boolean read, LocalDateTime createdAt) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitle(), n.getBody(), n.getType(), n.isRead(), n.getCreatedAt());
    }
}
