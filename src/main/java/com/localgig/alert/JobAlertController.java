package com.localgig.alert;

import com.localgig.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/job-alerts")
@Transactional
public class JobAlertController {
    private final JobAlertRepository alerts;
    public JobAlertController(JobAlertRepository alerts) { this.alerts = alerts; }
    @GetMapping
    public List<JobAlertResponse> mine(@AuthenticationPrincipal User user) {
        return alerts.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(JobAlertResponse::from).toList();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobAlertResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody JobAlertRequest request) {
        return JobAlertResponse.from(alerts.save(new JobAlert(user, request)));
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
