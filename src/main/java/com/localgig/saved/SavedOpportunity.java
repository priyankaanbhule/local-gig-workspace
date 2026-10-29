package com.localgig.saved;

import com.localgig.opportunity.Opportunity;
import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_opportunities", uniqueConstraints =
        @UniqueConstraint(name = "uk_saved_user_opportunity", columnNames = {"user_id", "opportunity_id"}))
public class SavedOpportunity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    protected SavedOpportunity() { }
    public SavedOpportunity(User user, Opportunity opportunity) { this.user = user; this.opportunity = opportunity; }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Opportunity getOpportunity() { return opportunity; }
}
