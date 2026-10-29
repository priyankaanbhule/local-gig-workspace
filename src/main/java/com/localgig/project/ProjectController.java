package com.localgig.project;

import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.user.User;
import com.localgig.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Transactional
public class ProjectController {
    private final ProjectRepository projects;
    private final NotificationRepository notifications;

    public ProjectController(ProjectRepository projects, NotificationRepository notifications) {
        this.projects = projects;
        this.notifications = notifications;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public List<ProjectResponse> mine(@AuthenticationPrincipal User user) {
        if (user.getRole() == UserRole.EMPLOYER || user.getRole() == UserRole.ADMIN) {
            return projects.findAllByEmployerIdOrderByCreatedAtDesc(user.getId()).stream().map(ProjectResponse::from).toList();
        }
        return projects.findAllByFreelancerIdOrderByCreatedAtDesc(user.getId()).stream().map(ProjectResponse::from).toList();
    }

    @PatchMapping("/{projectId}/progress")
    public ProjectResponse updateProgress(@AuthenticationPrincipal User user, @PathVariable Long projectId,
                                          @Valid @RequestBody ProjectProgressRequest request) {
        Project project = projects.findByIdAndFreelancerId(projectId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        try {
            if (request.deliverableUrl() != null && !request.deliverableUrl().isBlank()) {
                project.updateDeliverable(request.deliverableUrl());
            } else {
                project.updateProgress(request.progressPercent(), request.progressNote());
            }
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
        notifications.save(new Notification(project.getEmployer(), "Project progress updated",
                project.getFreelancer().getFullName() + " updated " + project.getProposal().getOpportunity().getTitle()
                        + " to " + project.getProgressPercent() + "%.", "PROJECT"));
        return ProjectResponse.from(project);
    }

    @PatchMapping("/{projectId}/complete")
    public ProjectResponse complete(@AuthenticationPrincipal User user, @PathVariable Long projectId) {
        Project project = projects.findByIdAndEmployerId(projectId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        try {
            project.complete();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
        notifications.save(new Notification(project.getFreelancer(), "Project marked complete",
                project.getProposal().getOpportunity().getTitle() + " has been marked complete.", "PROJECT"));
        return ProjectResponse.from(project);
    }
}
