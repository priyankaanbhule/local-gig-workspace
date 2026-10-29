package com.localgig.opportunity;

import com.localgig.application.ApplicationRepository;
import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.project.Project;
import com.localgig.project.ProjectRepository;
import com.localgig.project.ProjectStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class OpportunityExpiryService {
    private final OpportunityRepository opportunities;
    private final ApplicationRepository applications;
    private final NotificationRepository notifications;
    private final ProjectRepository projects;

    public OpportunityExpiryService(OpportunityRepository opportunities, ApplicationRepository applications,
                                    NotificationRepository notifications, ProjectRepository projects) {
        this.opportunities = opportunities;
        this.applications = applications;
        this.notifications = notifications;
        this.projects = projects;
    }

    @Scheduled(fixedDelayString = "${localgig.opportunities.expiry-check-ms:3600000}", initialDelay = 0)
    @Transactional
    public void expirePastDueOpportunities() {
        opportunities.expirePastDue(LocalDate.now());
        LocalDate reminderDate = LocalDate.now().plusDays(3);
        applications.findAllByDeadlineReminderSentFalse().stream()
                .filter(application -> application.getOpportunity().getDeadline() != null
                        && application.getOpportunity().getDeadline().equals(reminderDate)
                        && application.getStatus() != com.localgig.application.ApplicationStatus.REJECTED)
                .forEach(application -> {
                    notifications.save(new Notification(application.getApplicant(), "Application deadline soon",
                            "The deadline for “" + application.getOpportunity().getTitle() + "” is in 3 days.",
                            "DEADLINE"));
                    application.markDeadlineReminderSent();
                });
        projects.findAllByDeadlineReminderSentFalseAndStatus(ProjectStatus.IN_PROGRESS).stream()
                .filter(project -> project.getDueDate().equals(reminderDate))
                .forEach(project -> {
                    String title = project.getProposal().getOpportunity().getTitle();
                    String message = "The project “" + title + "” is due in 3 days.";
                    notifications.save(new Notification(project.getFreelancer(), "Project deadline soon", message, "PROJECT_DEADLINE"));
                    notifications.save(new Notification(project.getEmployer(), "Project deadline soon", message, "PROJECT_DEADLINE"));
                    project.markDeadlineReminderSent();
                });
    }
}
