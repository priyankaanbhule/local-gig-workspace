package com.localgig.application;

import com.localgig.user.UserRepository;
import com.localgig.message.DirectMessage;
import com.localgig.message.DirectMessageRepository;
import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Applies requested presentation names and removes the specifically identified mistaken message. */
@Component
public class PresentationAccountNamesInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final DirectMessageRepository directMessages;
    private final NotificationRepository notifications;

    public PresentationAccountNamesInitializer(UserRepository users, DirectMessageRepository directMessages,
                                               NotificationRepository notifications) {
        this.users = users;
        this.directMessages = directMessages;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        users.findByEmailIgnoreCase("Priyaanbhule486@gmail.com")
                .ifPresent(user -> user.setFullName("Job Seeker"));
        users.findByEmailIgnoreCase("shivanianbhule1@gmail.com")
                .ifPresent(user -> user.setFullName("Recruiter"));

        List<DirectMessage> mistakenMessages = directMessages.findAll().stream()
                .filter(message -> message.getBody().trim().equalsIgnoreCase("i want to connect"))
                .toList();
        for (DirectMessage message : mistakenMessages) {
            List<Notification> relatedNotifications = notifications
                    .findAllByRecipientIdOrderByCreatedAtDesc(message.getRecipient().getId()).stream()
                    .filter(notification -> notification.getType().equals("MESSAGE"))
                    .filter(notification -> notification.getBody().equalsIgnoreCase("i want to connect"))
                    .toList();
            notifications.deleteAll(relatedNotifications);
        }
        directMessages.deleteAll(mistakenMessages);
    }
}
