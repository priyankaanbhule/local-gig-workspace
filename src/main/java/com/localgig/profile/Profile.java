package com.localgig.profile;

import com.localgig.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String headline;

    @Column(length = 2000)
    private String summary;

    private String phone;
    private String city;
    private String country;
    private BigDecimal expectedSalary;
    private String portfolioUrl;
    private String resumeUrl;
    private String profilePictureUrl;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean available = true;

    @ElementCollection
    @CollectionTable(name = "profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_education", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "education", length = 500)
    private List<String> education = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_experience", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "experience", length = 500)
    private List<String> experience = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_projects", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "project", length = 500)
    private List<String> projects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_certifications", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "certification", length = 500)
    private List<String> certifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_preferred_roles", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "preferred_role")
    private List<String> preferredRoles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "profile_preferred_locations", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "preferred_location")
    private List<String> preferredLocations = new ArrayList<>();

    protected Profile() {
    }

    public Profile(User user) {
        this.user = user;
    }

    public void update(ProfileRequest request) {
        headline = request.headline();
        summary = request.summary();
        phone = request.phone();
        city = request.city();
        country = request.country();
        expectedSalary = request.expectedSalary();
        portfolioUrl = request.portfolioUrl();
        resumeUrl = request.resumeUrl();
        profilePictureUrl = request.profilePictureUrl();
        if (request.available() != null) available = request.available();

        skills = safeList(request.skills());
        education = safeList(request.education());
        experience = safeList(request.experience());
        projects = safeList(request.projects());
        certifications = safeList(request.certifications());
        preferredRoles = safeList(request.preferredRoles());
        preferredLocations = safeList(request.preferredLocations());
    }

    private List<String> safeList(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getHeadline() { return headline; }
    public String getSummary() { return summary; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public BigDecimal getExpectedSalary() { return expectedSalary; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public String getResumeUrl() { return resumeUrl; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public boolean isAvailable() { return available; }
    public List<String> getSkills() { return skills; }
    public List<String> getEducation() { return education; }
    public List<String> getExperience() { return experience; }
    public List<String> getProjects() { return projects; }
    public List<String> getCertifications() { return certifications; }
    public List<String> getPreferredRoles() { return preferredRoles; }
    public List<String> getPreferredLocations() { return preferredLocations; }
}
