package com.localgig.alert;

import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_alerts")
public class JobAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(length = 200) private String keywords;
    @Column(length = 120) private String location;
    @ElementCollection
    @CollectionTable(name = "job_alert_skills", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "skill", length = 80)
    private List<String> skills = new ArrayList<>();
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    protected JobAlert() { }
    public JobAlert(User user, JobAlertRequest request) { this.user = user; update(request); }
    public void update(JobAlertRequest request) {
        keywords = request.keywords() == null ? "" : request.keywords().trim();
        location = request.location() == null ? "" : request.location().trim();
        skills = request.skills() == null ? new ArrayList<>() : request.skills().stream()
                .filter(s -> s != null && !s.isBlank()).map(String::trim).distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getKeywords() { return keywords; }
    public String getLocation() { return location; }
    public List<String> getSkills() { return skills; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
