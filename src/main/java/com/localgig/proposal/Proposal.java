package com.localgig.proposal;

import com.localgig.opportunity.Opportunity;
import com.localgig.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposals", uniqueConstraints = @UniqueConstraint(
        name = "uk_proposal_freelancer_opportunity", columnNames = {"freelancer_id", "opportunity_id"}))
public class Proposal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(nullable = false, length = 3000)
    private String coverLetter;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal bidAmount;

    @Column(nullable = false)
    private Integer estimatedDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status = ProposalStatus.SUBMITTED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Proposal() { }

    public Proposal(User freelancer, Opportunity opportunity, ProposalRequest request) {
        this.freelancer = freelancer;
        this.opportunity = opportunity;
        this.coverLetter = request.coverLetter().trim();
        this.bidAmount = request.bidAmount();
        this.estimatedDays = request.estimatedDays();
    }

    public Long getId() { return id; }
    public User getFreelancer() { return freelancer; }
    public Opportunity getOpportunity() { return opportunity; }
    public String getCoverLetter() { return coverLetter; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public Integer getEstimatedDays() { return estimatedDays; }
    public ProposalStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setStatus(ProposalStatus status) { this.status = status; }
}
