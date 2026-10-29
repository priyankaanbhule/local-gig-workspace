package com.localgig.notification;

import com.localgig.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Transactional
public class NotificationController {
    private final NotificationRepository notifications;
    public NotificationController(NotificationRepository notifications) { this.notifications = notifications; }

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal User user) {
        return notifications.findAllByRecipientIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(NotificationResponse::from).toList();
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@AuthenticationPrincipal User user, @PathVariable Long id) {
        var item = notifications.findByIdAndRecipientId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        item.markRead();
        return NotificationResponse.from(item);
    }
}
