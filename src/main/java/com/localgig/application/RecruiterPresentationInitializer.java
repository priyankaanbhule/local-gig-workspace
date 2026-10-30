package com.localgig.application;

import com.localgig.message.DirectMessage;
import com.localgig.message.DirectMessageRepository;
import com.localgig.message.Message;
import com.localgig.message.MessageRepository;
import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.opportunity.Opportunity;
import com.localgig.opportunity.OpportunityRepository;
import com.localgig.opportunity.OpportunityRequest;
import com.localgig.user.User;
import com.localgig.user.UserRepository;
import com.localgig.user.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Seeds a clearly labeled, interactive local hiring walkthrough when demo data is enabled. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(name = "localgig.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class RecruiterPresentationInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final OpportunityRepository opportunities;
    private final ApplicationRepository applications;
    private final NotificationRepository notifications;
    private final MessageRepository messages;
    private final DirectMessageRepository directMessages;
    private final PasswordEncoder passwordEncoder;

    public RecruiterPresentationInitializer(UserRepository users, OpportunityRepository opportunities,
            ApplicationRepository applications, NotificationRepository notifications,
            MessageRepository messages, DirectMessageRepository directMessages,
            PasswordEncoder passwordEncoder) {
        this.users = users;
        this.opportunities = opportunities;
        this.applications = applications;
        this.notifications = notifications;
        this.messages = messages;
        this.directMessages = directMessages;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User employer = users.findAll().stream()
                .filter(user -> user.getRole() == UserRole.EMPLOYER)
                .filter(user -> !user.getEmail().endsWith("@localgig.local"))
                .findFirst().orElse(null);
        if (employer == null) return;

        Opportunity opportunity = opportunities.findAllByEmployerIdOrderByCreatedAtDesc(employer.getId()).stream()
                .filter(item -> item.getTitle().equals("Software Engineer · Presentation role"))
                .findFirst().orElseGet(() -> opportunities.save(new Opportunity(employer,
                        new OpportunityRequest("Software Engineer · Presentation role",
                                "LocalGig Presentation Workspace", "Pune, Maharashtra", "Software Development",
                                "JOB", "Full-time", "₹5–7 LPA",
                                "Local walkthrough role for a Java and Spring Boot engineer. Review candidate applications, update their status, and connect through the inbox.",
                                List.of("Java", "Spring Boot", "PostgreSQL", "REST API"),
                                LocalDate.now().plusDays(30), 18.5204, 73.8567, 1))));

        seedApplication(employer, opportunity, "Anaya Deshmukh", "sample-java-pune@localgig.local", ApplicationStatus.SUBMITTED);
        seedApplication(employer, opportunity, "Rohan Kulkarni", "sample-java-pune-2@localgig.local", ApplicationStatus.SUBMITTED);
        seedApplication(employer, opportunity, "Meera Joshi", "sample-frontend-pune@localgig.local", ApplicationStatus.SELECTED);
        seedApplication(employer, opportunity, "Kavya Patil", "sample-kavya-pune@localgig.local", ApplicationStatus.REJECTED);

        List<DirectMessage> mistakenMessages = directMessages.findAllBySenderIdOrRecipientIdOrderByCreatedAtDesc(employer.getId(), employer.getId())
                .stream().filter(message -> message.getSender().getId().equals(employer.getId())
                        && message.getBody().trim().equalsIgnoreCase("i want to connect"))
                .toList();
        directMessages.deleteAll(mistakenMessages);
        mistakenMessages.forEach(message -> {
            List<Notification> alerts = notifications.findAllByRecipientIdOrderByCreatedAtDesc(message.getRecipient().getId()).stream()
                    .filter(item -> item.getType().equals("MESSAGE"))
                    .filter(item -> item.getTitle().equals("New message from " + employer.getFullName()))
                    .filter(item -> item.getBody().equals(message.getBody()))
                    .toList();
            notifications.deleteAll(alerts);
        });
    }

    private void seedApplication(User employer, Opportunity opportunity, String name, String email,
                                 ApplicationStatus status) {
        User candidate = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User created = new User(name, email, passwordEncoder.encode(UUID.randomUUID().toString()), UserRole.JOB_SEEKER);
            created.markEmailVerified();
            return users.save(created);
        });
        if (applications.existsByApplicantIdAndOpportunityId(candidate.getId(), opportunity.getId())) return;

        JobApplication application = new JobApplication(candidate, opportunity,
                new ApplicationRequest("Presentation candidate profile. This application is local demo data.", ""));
        application.setStatus(status);
        applications.save(application);
        notifications.save(new Notification(employer, "New application received",
                name + " applied for " + opportunity.getTitle() + ". Review this presentation candidate in your hiring pipeline.", "APPLICATION"));

        if (status == ApplicationStatus.SELECTED || status == ApplicationStatus.REJECTED) {
            boolean accepted = status == ApplicationStatus.SELECTED;
            notifications.save(new Notification(candidate, accepted ? "Application accepted" : "Application update",
                    "Your application for " + opportunity.getTitle() + (accepted ? " has been accepted." : " was not selected this time."), "APPLICATION"));
            messages.save(new Message(application, employer, accepted
                    ? "Thank you for applying. We would be pleased to discuss the next steps for the Software Engineer role. Please reply here with a suitable time."
                    : "Thank you for your interest in the Software Engineer role. We have decided to proceed with other applicants for this position. We appreciate the time you invested in applying."));
        }
    }
}
