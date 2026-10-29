package com.localgig.opportunity;

import com.localgig.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "opportunities")
public class Opportunity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 160)
    private String company;

    @Column(nullable = false, length = 120)
    private String location;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 30)
    private String kind;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer requiredExperienceYears = 0;

    @Column(nullable = false, length = 120)
    private String budget;

    @Column(nullable = false, length = 5000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "opportunity_skills", joinColumns = @JoinColumn(name = "opportunity_id"))
    @Column(name = "skill", nullable = false, length = 80)
    private List<String> skills = new ArrayList<>();

    private LocalDate deadline;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Opportunity() { }

    public Opportunity(User employer, OpportunityRequest request) {
        this.employer = employer;
        update(request);
    }

    public void update(OpportunityRequest request) {
        title = request.title().trim();
        company = request.company().trim();
        location = request.location().trim();
        latitude = request.latitude();
        longitude = request.longitude();
        category = request.category().trim();
        kind = request.kind().trim().toUpperCase();
        type = request.type().trim();
        requiredExperienceYears = request.requiredExperienceYears() == null ? 0 : request.requiredExperienceYears();
        budget = request.budget().trim();
        description = request.description().trim();
        deadline = request.deadline();
        skills = request.skills() == null ? new ArrayList<>() : request.skills().stream()
                .filter(s -> s != null && !s.isBlank()).map(String::trim).distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    public void close() { status = "CLOSED"; }
    public void reopen() { status = "ACTIVE"; }
    public Long getId() { return id; }
    public User getEmployer() { return employer; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getCategory() { return category; }
    public String getKind() { return kind; }
    public String getType() { return type; }
    public Integer getRequiredExperienceYears() { return requiredExperienceYears; }
    public String getBudget() { return budget; }
    public String getDescription() { return description; }
    public List<String> getSkills() { return skills; }
    public LocalDate getDeadline() { return deadline; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
