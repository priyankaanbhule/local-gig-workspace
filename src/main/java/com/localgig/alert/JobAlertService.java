package com.localgig.alert;

import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.opportunity.Opportunity;
import com.localgig.opportunity.OpportunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.Locale;

@Service
public class JobAlertService {
    private final JobAlertRepository alerts;
    private final NotificationRepository notifications;
    private final OpportunityRepository opportunities;
    public JobAlertService(JobAlertRepository alerts, NotificationRepository notifications,
                           OpportunityRepository opportunities) {
        this.alerts = alerts;
        this.notifications = notifications;
        this.opportunities = opportunities;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyAlertCreated(JobAlert alert) {
        String criteria = describeCriteria(alert);
        notifications.save(new Notification(alert.getUser(), "Job alert created",
                "Your alert for " + criteria + " is active. We’ll notify you when matching opportunities are posted.",
                "JOB_ALERT"));

        opportunities.findAllByStatusOrderByCreatedAtDesc("ACTIVE").stream()
                .filter(opportunity -> matches(alert, opportunity))
                .limit(2)
                .forEach(opportunity -> notifyMatch(alert, opportunity));
    }

    @Transactional
    public void notifyMatching(Opportunity opportunity) {
        for (JobAlert alert : alerts.findAllByActiveTrue()) {
            if (matches(alert, opportunity)) notifyMatch(alert, opportunity);
        }
    }

    private boolean matches(JobAlert alert, Opportunity opportunity) {
        String location = opportunity.getLocation() == null ? "" : opportunity.getLocation();
        boolean locationMatches = blank(alert.getLocation()) || location.toLowerCase(Locale.ROOT)
                .contains(alert.getLocation().toLowerCase(Locale.ROOT));
        String searchable = (opportunity.getTitle() + " " + opportunity.getCompany() + " "
                + opportunity.getDescription() + " " + String.join(" ", opportunity.getSkills())).toLowerCase(Locale.ROOT);
        boolean keywordMatches = blank(alert.getKeywords()) || searchable.contains(alert.getKeywords().toLowerCase(Locale.ROOT));
        boolean skillMatches = alert.getSkills().isEmpty() || alert.getSkills().stream()
                .filter(skill -> !blank(skill))
                .anyMatch(skill -> searchable.contains(skill.toLowerCase(Locale.ROOT)));
        return locationMatches && keywordMatches && skillMatches;
    }

    private void notifyMatch(JobAlert alert, Opportunity opportunity) {
        notifications.save(new Notification(alert.getUser(), "A new opportunity matches your alert",
                opportunity.getTitle() + " at " + opportunity.getCompany() + " in " + opportunity.getLocation()
                        + " is accepting applications. View the listing in LocalGig Workspace.", "JOB_ALERT"));
    }

    private String describeCriteria(JobAlert alert) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (!blank(alert.getKeywords())) parts.add("“" + alert.getKeywords() + "”");
        if (!blank(alert.getLocation())) parts.add("opportunities in " + alert.getLocation());
        if (!alert.getSkills().isEmpty()) parts.add("skills " + String.join(", ", alert.getSkills()));
        return parts.isEmpty() ? "all opportunities" : String.join(" · ", parts);
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
