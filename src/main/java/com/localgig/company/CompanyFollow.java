package com.localgig.company;

import com.localgig.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_follows", uniqueConstraints =
        @UniqueConstraint(name = "uk_company_follow_follower_company",
                columnNames = {"follower_id", "company_id"}))
public class CompanyFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private User company;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected CompanyFollow() { }

    public CompanyFollow(User follower, User company) {
        this.follower = follower;
        this.company = company;
    }

    public Long getId() { return id; }
    public User getFollower() { return follower; }
    public User getCompany() { return company; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
