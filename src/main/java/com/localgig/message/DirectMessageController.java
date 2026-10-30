package com.localgig.message;

import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.user.User;
import com.localgig.user.UserRepository;
import com.localgig.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages/direct")
@Transactional
public class DirectMessageController {
    private final DirectMessageRepository messages;
    private final UserRepository users;
    private final NotificationRepository notifications;

    public DirectMessageController(DirectMessageRepository messages, UserRepository users,
                                   NotificationRepository notifications) {
        this.messages = messages;
        this.users = users;
        this.notifications = notifications;
    }

    @GetMapping("/conversations")
    public List<DirectConversationResponse> conversations(@AuthenticationPrincipal User user) {
        Map<Long, List<DirectMessage>> grouped = messages
                .findAllBySenderIdOrRecipientIdOrderByCreatedAtDesc(user.getId(), user.getId()).stream()
                .collect(Collectors.groupingBy(message -> peer(message, user).getId()));
        return grouped.entrySet().stream()
                .map(entry -> {
                    List<DirectMessage> thread = entry.getValue();
                    DirectMessage latest = thread.getFirst();
                    User other = peer(latest, user);
                    long unread = thread.stream().filter(message -> message.getRecipient().getId().equals(user.getId()))
                            .filter(message -> !message.isRead()).count();
                    return new DirectConversationResponse(other.getId(), other.getFullName(),
                            other.getRole().name(), latest.getBody(), latest.getCreatedAt(), unread);
                })
                .sorted(Comparator.comparing(DirectConversationResponse::createdAt).reversed())
                .toList();
    }

    @GetMapping
    public List<DirectMessageResponse> list(@AuthenticationPrincipal User user, @RequestParam Long userId) {
        User other = findPeer(userId);
        requireConversationRoles(user, other);
        List<DirectMessage> thread = messages
                .findAllBySenderIdOrRecipientIdOrderByCreatedAtDesc(user.getId(), user.getId()).stream()
                .filter(message -> isPair(message, user.getId(), other.getId()))
                .sorted(Comparator.comparing(DirectMessage::getCreatedAt))
                .toList();
        thread.stream().filter(message -> message.getRecipient().getId().equals(user.getId()))
                .forEach(DirectMessage::markRead);
        return thread.stream().map(DirectMessageResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectMessageResponse send(@AuthenticationPrincipal User user,
                                      @Valid @RequestBody DirectMessageRequest request) {
        User recipient = findPeer(request.recipientId());
        requireConversationRoles(user, recipient);
        DirectMessage saved = messages.save(new DirectMessage(user, recipient, request.body().trim()));
        notifications.save(new Notification(recipient, "New message from " + user.getFullName(),
                request.body().trim().length() > 160 ? request.body().trim().substring(0, 157) + "…" : request.body().trim(),
                "MESSAGE"));
        return DirectMessageResponse.from(saved);
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwnMessage(@AuthenticationPrincipal User user, @PathVariable Long messageId) {
        DirectMessage message = messages.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
        if (!message.getSender().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can delete only messages you sent");
        }
        List<Notification> relatedAlerts = notifications.findAllByRecipientIdOrderByCreatedAtDesc(message.getRecipient().getId())
                .stream()
                .filter(item -> item.getType().equals("MESSAGE"))
                .filter(item -> item.getTitle().equals("New message from " + user.getFullName()))
                .filter(item -> item.getBody().equals(message.getBody()))
                .toList();
        notifications.deleteAll(relatedAlerts);
        messages.delete(message);
    }

    private User findPeer(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void requireConversationRoles(User first, User second) {
        if (first.getId().equals(second.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot message yourself");
        boolean firstEmployer = first.getRole() == UserRole.EMPLOYER || first.getRole() == UserRole.ADMIN;
        boolean secondEmployer = second.getRole() == UserRole.EMPLOYER || second.getRole() == UserRole.ADMIN;
        boolean firstCandidate = first.getRole() == UserRole.JOB_SEEKER || first.getRole() == UserRole.FREELANCER;
        boolean secondCandidate = second.getRole() == UserRole.JOB_SEEKER || second.getRole() == UserRole.FREELANCER;
        if (!(firstEmployer && secondCandidate) && !(secondEmployer && firstCandidate))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Direct messages are available between recruiters and candidates");
    }

    private User peer(DirectMessage message, User user) {
        return message.getSender().getId().equals(user.getId()) ? message.getRecipient() : message.getSender();
    }

    private boolean isPair(DirectMessage message, Long firstId, Long secondId) {
        Long senderId = message.getSender().getId();
        Long recipientId = message.getRecipient().getId();
        return (senderId.equals(firstId) && recipientId.equals(secondId))
                || (senderId.equals(secondId) && recipientId.equals(firstId));
    }
}
