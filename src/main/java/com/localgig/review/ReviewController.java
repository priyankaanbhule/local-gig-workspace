package com.localgig.review;

import com.localgig.application.ApplicationRepository;
import com.localgig.application.ApplicationStatus;
import com.localgig.application.JobApplication;
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
@RequestMapping("/api/reviews")
@Transactional
public class ReviewController {
    private final ReviewRepository reviews;
    private final ProjectReviewRepository projectReviews;
    private final ApplicationRepository applications;
    private final ProjectRepository projects;
    public ReviewController(ReviewRepository reviews, ProjectReviewRepository projectReviews,
                            ApplicationRepository applications, ProjectRepository projects) {
        this.reviews = reviews;
        this.projectReviews = projectReviews;
        this.applications = applications;
        this.projects = projects;
    }

    @GetMapping("/users/{userId}")
    public List<ReviewResponse> forUser(@PathVariable Long userId) {
        return java.util.stream.Stream.concat(
                        reviews.findAllByRevieweeIdOrderByCreatedAtDesc(userId).stream().map(ReviewResponse::from),
                        projectReviews.findAllByRevieweeIdOrderByCreatedAtDesc(userId).stream().map(ReviewResponse::from))
                .sorted(java.util.Comparator.comparing(ReviewResponse::createdAt).reversed())
                .toList();
    }

    @GetMapping("/me/projects")
    public List<ReviewResponse> myProjectReviews(@AuthenticationPrincipal User user) {
        return projectReviews.findAllByReviewerIdOrderByCreatedAtDesc(user.getId())
                .stream().map(ReviewResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody ReviewRequest request) {
        JobApplication application = applications.findById(request.applicationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (application.getStatus() != ApplicationStatus.COMPLETED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reviews are available after project completion");
        Long applicantId = application.getApplicant().getId();
        Long employerId = application.getOpportunity().getEmployer().getId();
        User reviewee;
        if (user.getId().equals(applicantId)) reviewee = application.getOpportunity().getEmployer();
        else if (user.getId().equals(employerId)) reviewee = application.getApplicant();
        else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project participants can review each other");
        if (reviews.existsByApplicationIdAndReviewerId(application.getId(), user.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reviewed this project");
        String body = request.body() == null || request.body().isBlank() ? "" : request.body().trim();
        return ReviewResponse.from(reviews.save(new Review(application, user, reviewee, request.rating(), body)));
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createForProject(@AuthenticationPrincipal User user,
                                           @Valid @RequestBody ProjectReviewRequest request) {
        Project project = projects.findById(request.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (project.getStatus() != com.localgig.project.ProjectStatus.COMPLETED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reviews are available after project completion");

        User reviewee;
        if (user.getId().equals(project.getFreelancer().getId())) reviewee = project.getEmployer();
        else if (user.getId().equals(project.getEmployer().getId())) reviewee = project.getFreelancer();
        else throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only project participants can review each other");

        if (projectReviews.existsByProjectIdAndReviewerId(project.getId(), user.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reviewed this project");
        String body = request.body() == null ? "" : request.body().trim();
        return ReviewResponse.from(projectReviews.save(new ProjectReview(project, user, reviewee, request.rating(), body)));
    }
}
