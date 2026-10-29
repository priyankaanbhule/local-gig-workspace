package com.localgig.message;

import com.localgig.notification.Notification;
import com.localgig.notification.NotificationRepository;
import com.localgig.project.Project;
import com.localgig.project.ProjectRepository;
import com.localgig.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/messages/projects/{projectId}")
@Transactional
public class ProjectMessageController {
    private final ProjectRepository projects;
    private final ProjectMessageRepository messages;
    private final NotificationRepository notifications;

    public ProjectMessageController(ProjectRepository projects, ProjectMessageRepository messages,
                                    NotificationRepository notifications) {
        this.projects = projects;
        this.messages = messages;
        this.notifications = notifications;
    }

    @GetMapping
    public List<ProjectMessageResponse> list(@AuthenticationPrincipal User user, @PathVariable Long projectId) {
        Project project = accessibleProject(user, projectId);
        List<ProjectMessage> thread = messages.findAllByProjectIdOrderByCreatedAtAsc(project.getId());
        thread.stream().filter(message -> !message.getSender().getId().equals(user.getId()))
                .forEach(ProjectMessage::markRead);
        return thread.stream().map(ProjectMessageResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMessageResponse send(@AuthenticationPrincipal User user, @PathVariable Long projectId,
                                       @Valid @RequestBody ProjectMessageRequest request) {
        Project project = accessibleProject(user, projectId);
        ProjectMessage message = messages.save(new ProjectMessage(project, user, request.body().trim()));
        User recipient = project.getFreelancer().getId().equals(user.getId())
                ? project.getEmployer() : project.getFreelancer();
        notifications.save(new Notification(recipient, "New project message",
                "You have a new message about " + project.getProposal().getOpportunity().getTitle() + ".", "MESSAGE"));
        return ProjectMessageResponse.from(message);
    }

    private Project accessibleProject(User user, Long projectId) {
        Project project = projects.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project conversation not found"));
        if (!project.getFreelancer().getId().equals(user.getId())
                && !project.getEmployer().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this conversation");
        }
        return project;
    }
}
