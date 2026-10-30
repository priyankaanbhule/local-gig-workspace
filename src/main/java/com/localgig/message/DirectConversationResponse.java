package com.localgig.message;

import java.time.LocalDateTime;

public record DirectConversationResponse(Long userId, String fullName, String role,
                                         String lastMessage, LocalDateTime createdAt,
                                         long unreadCount) { }
