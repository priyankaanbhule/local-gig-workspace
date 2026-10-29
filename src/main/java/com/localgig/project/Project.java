package com.localgig.project;

import com.localgig.proposal.Proposal;
import com.localgig.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", nullable = false, unique = true)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.IN_PROGRESS;

    @Column(nullable = false)
    private int progressPercent;

    @Column(length = 2000)
    private String progressNote;

    @Column(length = 500)
    private String deliverableUrl;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deadlineReminderSent;

    protected Project() { }

    public Project(Proposal proposal) {
        this.proposal = proposal;
        this.freelancer = proposal.getFreelancer();
        this.employer = proposal.getOpportunity().getEmployer();
        this.dueDate = LocalDate.now().plusDays(proposal.getEstimatedDays());
    }

    public void updateProgress(int progressPercent, String note) {
        if (status == ProjectStatus.COMPLETED) throw new IllegalStateException("Completed projects cannot be changed");
        this.progressPercent = progressPercent;
        this.progressNote = note == null ? "" : note.trim();
        this.status = progressPercent == 100 ? ProjectStatus.SUBMITTED : ProjectStatus.IN_PROGRESS;
    }

    public void updateDeliverable(String deliverableUrl) {
        if (status == ProjectStatus.COMPLETED) throw new IllegalStateException("Completed projects cannot be changed");
        this.deliverableUrl = deliverableUrl == null ? "" : deliverableUrl.trim();
        this.progressPercent = 100;
        this.status = ProjectStatus.SUBMITTED;
    }

    public void complete() {
        if (status != ProjectStatus.SUBMITTED) throw new IllegalStateException("The freelancer must submit completed work first");
        status = ProjectStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Proposal getProposal() { return proposal; }
    public User getFreelancer() { return freelancer; }
    public User getEmployer() { return employer; }
    public ProjectStatus getStatus() { return status; }
    public int getProgressPercent() { return progressPercent; }
    public String getProgressNote() { return progressNote; }
    public String getDeliverableUrl() { return deliverableUrl; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public boolean isDeadlineReminderSent() { return deadlineReminderSent; }
    public void markDeadlineReminderSent() { deadlineReminderSent = true; }
}
