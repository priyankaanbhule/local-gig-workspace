package com.localgig.message;

import com.localgig.application.ApplicationRepository;
import com.localgig.application.JobApplication;
import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Transactional
public class MessageController {
    private final MessageRepository messages;
    private final ApplicationRepository applications;
    private final NotificationRepository notifications;
    public MessageController(MessageRepository messages, ApplicationRepository applications,
                             NotificationRepository notifications) {
        this.messages = messages; this.applications = applications; this.notifications = notifications;
    }

    @GetMapping
    public List<MessageResponse> list(@AuthenticationPrincipal User user, @RequestParam Long applicationId) {
        JobApplication application = accessibleApplication(user, applicationId);
        List<Message> thread = messages.findAllByApplicationIdOrderByCreatedAtAsc(application.getId());
        thread.stream().filter(m -> !m.getSender().getId().equals(user.getId())).forEach(Message::markRead);
        return thread.stream().map(MessageResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@AuthenticationPrincipal User user, @Valid @RequestBody MessageRequest request) {
        JobApplication application = accessibleApplication(user, request.applicationId());
        Message message = messages.save(new Message(application, user, request.body().trim()));
        User recipient = application.getApplicant().getId().equals(user.getId())
                ? application.getOpportunity().getEmployer() : application.getApplicant();
        notifications.save(new Notification(recipient, "New message", "You have a new LocalGig message.", "MESSAGE"));
        return MessageResponse.from(message);
    }

    private JobApplication accessibleApplication(User user, Long id) {
        JobApplication application = applications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application conversation not found"));
        Long applicantId = application.getApplicant().getId();
        Long employerId = application.getOpportunity().getEmployer().getId();
        if (!user.getId().equals(applicantId) && !user.getId().equals(employerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this conversation");
        }
        return application;
    }
}
