package com.localgig.payment;

import com.localgig.application.ApplicationRepository;
import com.localgig.application.ApplicationStatus;
import com.localgig.application.JobApplication;
import com.localgig.project.Project;
import com.localgig.project.ProjectRepository;
import com.localgig.project.ProjectStatus;
import com.localgig.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Transactional
public class PaymentController {
    private final PaymentRepository payments;
    private final ProjectPaymentRepository projectPayments;
    private final ApplicationRepository applications;
    private final ProjectRepository projects;
    public PaymentController(PaymentRepository payments, ProjectPaymentRepository projectPayments,
                             ApplicationRepository applications, ProjectRepository projects) {
        this.payments = payments;
        this.projectPayments = projectPayments;
        this.applications = applications;
        this.projects = projects;
    }
    @GetMapping
    public List<PaymentResponse> list(@AuthenticationPrincipal User user) {
        return java.util.stream.Stream.concat(
                        payments.findForUser(user.getId()).stream().map(PaymentResponse::from),
                        projectPayments.findForUser(user.getId()).stream().map(PaymentResponse::from))
                .sorted(java.util.Comparator.comparing(PaymentResponse::createdAt).reversed())
                .toList();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody PaymentRequest request) {
        JobApplication application = applications.findById(request.applicationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!application.getOpportunity().getEmployer().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can create a payment record");
        if (application.getStatus() != ApplicationStatus.COMPLETED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment records are available for completed projects");
        if (!"GIG".equalsIgnoreCase(application.getOpportunity().getKind()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment records are only available for completed gigs");
        if (payments.findByApplicationId(application.getId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A payment record already exists for this project");
        return PaymentResponse.from(payments.save(new PaymentRecord(application, request.amount())));
    }
    @PatchMapping("/{id}/mark-paid")
    public PaymentResponse markPaid(@AuthenticationPrincipal User user, @PathVariable Long id) {
        PaymentRecord record = payments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment record not found"));
        if (!record.getApplication().getOpportunity().getEmployer().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can update this record");
        record.markPaid();
        return PaymentResponse.from(record);
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createForProject(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody ProjectPaymentRequest request) {
        Project project = projects.findByIdAndEmployerId(request.projectId(), user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Completed project not found"));
        if (project.getStatus() != ProjectStatus.COMPLETED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment records are available after project completion");
        if (!"GIG".equalsIgnoreCase(project.getProposal().getOpportunity().getKind()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment records are only available for completed gigs");
        if (projectPayments.findByProjectId(project.getId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A payment record already exists for this project");
        return PaymentResponse.from(projectPayments.save(new ProjectPaymentRecord(project, request.amount())));
    }

    @PatchMapping("/projects/{id}/mark-paid")
    public PaymentResponse markProjectPaid(@AuthenticationPrincipal User user, @PathVariable Long id) {
        ProjectPaymentRecord record = projectPayments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment record not found"));
        if (!record.getProject().getEmployer().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can update this record");
        record.markPaid();
        return PaymentResponse.from(record);
    }
}
