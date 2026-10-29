package com.localgig.alert;

import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.opportunity.Opportunity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class JobAlertService {
    private final JobAlertRepository alerts;
    private final NotificationRepository notifications;
    public JobAlertService(JobAlertRepository alerts, NotificationRepository notifications) {
        this.alerts = alerts; this.notifications = notifications;
    }
    @Transactional
    public void notifyMatching(Opportunity opportunity) {
        for (JobAlert alert : alerts.findAllByActiveTrue()) {
            boolean locationMatches = blank(alert.getLocation()) || opportunity.getLocation().toLowerCase(Locale.ROOT)
                    .contains(alert.getLocation().toLowerCase(Locale.ROOT));
            String searchable = (opportunity.getTitle() + " " + opportunity.getCompany() + " "
                    + opportunity.getDescription() + " " + String.join(" ", opportunity.getSkills())).toLowerCase(Locale.ROOT);
            boolean keywordMatches = blank(alert.getKeywords()) || searchable.contains(alert.getKeywords().toLowerCase(Locale.ROOT));
            boolean skillMatches = alert.getSkills().isEmpty() || alert.getSkills().stream()
                    .anyMatch(skill -> searchable.contains(skill.toLowerCase(Locale.ROOT)));
            if (locationMatches && keywordMatches && skillMatches) {
                notifications.save(new Notification(alert.getUser(), "New opportunity matches your alert",
                        opportunity.getTitle() + " · " + opportunity.getLocation(), "JOB_ALERT"));
            }
        }
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
