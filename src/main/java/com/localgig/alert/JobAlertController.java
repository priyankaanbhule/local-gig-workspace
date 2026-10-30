package com.localgig.alert;

import com.localgig.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/job-alerts")
@Transactional
public class JobAlertController {
    private static final Logger log = LoggerFactory.getLogger(JobAlertController.class);
    private final JobAlertRepository alerts;
    private final JobAlertService jobAlertService;
    public JobAlertController(JobAlertRepository alerts, JobAlertService jobAlertService) {
        this.alerts = alerts;
        this.jobAlertService = jobAlertService;
    }
    @GetMapping
    public List<JobAlertResponse> mine(@AuthenticationPrincipal User user) {
        return alerts.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(JobAlertResponse::from).toList();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobAlertResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody JobAlertRequest request) {
        JobAlert alert = alerts.save(new JobAlert(user, request));
        try {
            jobAlertService.notifyAlertCreated(alert);
        } catch (RuntimeException exception) {
            // The alert itself should still be saved if an optional notification cannot be written.
            log.error("Alert {} was saved, but its notification could not be created", alert.getId(), exception);
        }
        return JobAlertResponse.from(alert);
    }
    @PutMapping("/{id}")
    public JobAlertResponse update(@AuthenticationPrincipal User user, @PathVariable Long id,
                                  @Valid @RequestBody JobAlertRequest request) {
        JobAlert alert = alerts.findById(id).filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found"));
        alert.update(request);
        return JobAlertResponse.from(alert);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        JobAlert alert = alerts.findById(id).filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found"));
        alerts.delete(alert);
    }
}
