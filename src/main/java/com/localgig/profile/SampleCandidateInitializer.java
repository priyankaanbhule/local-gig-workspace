package com.localgig.profile;

import com.localgig.user.User;
import com.localgig.user.UserRepository;
import com.localgig.user.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Creates clearly marked local sample candidates so recruiters can demo talent search. */
@Component
@ConditionalOnProperty(name = "localgig.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class SampleCandidateInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final ProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;

    public SampleCandidateInitializer(UserRepository users, ProfileRepository profiles,
                                      PasswordEncoder passwordEncoder) {
        this.users = users;
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createCandidate("Anaya Deshmukh", "sample-java-pune@localgig.local",
                "Demo profile · Junior Java Developer", "Java developer building Spring Boot APIs and PostgreSQL-backed applications.",
                List.of("Java", "Spring Boot", "PostgreSQL", "REST APIs", "Git"),
                List.of("Java Developer", "Backend Developer"), List.of("Pune", "Remote"),
                List.of("Java Developer Intern · Built REST endpoints and wrote integration documentation."));
        createCandidate("Rohan Kulkarni", "sample-java-pune-2@localgig.local",
                "Demo profile · Backend Developer", "Entry-level backend developer interested in reliable services and clean API design.",
                List.of("Java", "Spring Boot", "SQL", "Hibernate", "Docker"),
                List.of("Backend Developer", "Software Engineer"), List.of("Pune", "Pimpri-Chinchwad"),
                List.of("Academic project · Created a Spring Boot inventory API with PostgreSQL."));
        createCandidate("Meera Joshi", "sample-frontend-pune@localgig.local",
                "Demo profile · Full-stack Developer", "Early-career developer who enjoys building clear user experiences and backend integrations.",
                List.of("Java", "React", "JavaScript", "Spring Boot", "CSS"),
                List.of("Full-stack Developer", "Java Developer"), List.of("Pune", "Remote"),
                List.of("Portfolio project · Built a React dashboard backed by Java REST services."));
        createCandidate("Kavya Patil", "sample-kavya-pune@localgig.local",
                "Demo profile · Software Engineer", "Early-career software engineer with experience building Java services and collaborative product features.",
                List.of("Java", "Spring Boot", "PostgreSQL", "REST APIs", "Git"),
                List.of("Software Engineer", "Backend Developer"), List.of("Pune", "Remote"),
                List.of("Project work · Designed a Java REST service with PostgreSQL persistence."));
    }

    private void createCandidate(String name, String email, String headline, String summary,
                                List<String> skills, List<String> roles, List<String> locations,
                                List<String> experience) {
        User candidate = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User sample = new User(name, email, passwordEncoder.encode(UUID.randomUUID().toString()), UserRole.JOB_SEEKER);
            sample.markEmailVerified();
            return users.save(sample);
        });
        if (profiles.findByUserId(candidate.getId()).isPresent()) return;
        Profile profile = new Profile(candidate);
        profile.update(new ProfileRequest(headline, summary, null, "Pune", "India", skills,
                List.of("Bachelor of Engineering · Computer Science"), experience,
                List.of("LocalGig sample candidate portfolio"), List.of(), roles, locations,
                null, null, null, null, true));
        profiles.save(profile);
    }
}
