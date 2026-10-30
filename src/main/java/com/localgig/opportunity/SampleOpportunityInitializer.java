package com.localgig.opportunity;

import com.localgig.user.User;
import com.localgig.user.UserRepository;
import com.localgig.user.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Adds clearly labeled local sample listings when the database has none. */
@Component
@ConditionalOnProperty(name = "localgig.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class SampleOpportunityInitializer implements ApplicationRunner {
    private final OpportunityRepository opportunities;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public SampleOpportunityInitializer(OpportunityRepository opportunities, UserRepository users,
                                        PasswordEncoder passwordEncoder) {
        this.opportunities = opportunities;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User employer = users.findByEmailIgnoreCase("sample-employer@localgig.local").orElseGet(() -> {
            User sample = new User("LocalGig Sample Employer", "sample-employer@localgig.local",
                    passwordEncoder.encode(UUID.randomUUID().toString()), UserRole.EMPLOYER);
            sample.markEmailVerified();
            return users.save(sample);
        });

        LocalDate today = LocalDate.now();
        List<Opportunity> showcaseListings = List.of(
                listing(employer, "Junior Java Developer", "Sample: NovaTech Solutions", "Pune, Maharashtra",
                        "Software Development", "JOB", "Full-time", "₹4–6 LPA",
                        "Work on Java and Spring Boot features with an experienced product team. Open to recent graduates.",
                        List.of("Java", "Spring Boot", "PostgreSQL", "REST API"), today.plusDays(30), 0, 18.5204, 73.8567),
                listing(employer, "Backend Developer Intern", "Sample: BrightPath Labs", "Pimpri-Chinchwad, Maharashtra",
                        "Software Development", "INTERNSHIP", "Internship", "₹12,000–18,000 / month",
                        "A guided backend internship building and documenting APIs. Mentorship and a completion letter included.",
                        List.of("Java", "Spring Boot", "SQL", "Git"), today.plusDays(25), 0, 18.6298, 73.7997),
                listing(employer, "Digital Marketing Associate", "Sample: MangoTree Media", "Mumbai, Maharashtra",
                        "Marketing", "JOB", "Full-time", "₹3.5–5 LPA",
                        "Plan social campaigns, write content, and review campaign performance with the marketing team.",
                        List.of("SEO", "Social Media", "Content Writing", "Analytics"), today.plusDays(21), 1, 19.0760, 72.8777),
                listing(employer, "Graphic Designer for Brand Kit", "Sample: PixelCraft Studio", "Remote",
                        "Design", "GIG", "Freelance", "₹8,000 fixed-price",
                        "Create a compact brand kit with logo variations, social templates, and source files. Remote collaboration.",
                        List.of("Graphic Design", "Figma", "Adobe Illustrator", "Branding"), today.plusDays(20), 0, null, null),
                listing(employer, "Customer Support Executive", "Sample: Neighborly Services", "Pune, Maharashtra",
                        "Customer Support", "JOB", "Part-time", "₹18,000–24,000 / month",
                        "Help customers by chat and email, record common issues, and work with the operations team.",
                        List.of("Communication", "Email Support", "Customer Service"), today.plusDays(28), 0, 18.5204, 73.8567),
                listing(employer, "Spreadsheet Cleanup and Data Entry", "Sample: ClearDesk Projects", "Remote",
                        "Operations", "GIG", "Freelance", "₹3,000–5,000 fixed-price",
                        "Clean and organize a spreadsheet, check entries for duplicates, and return a short completion summary.",
                        List.of("Excel", "Google Sheets", "Data Entry", "Attention to Detail"), today.plusDays(14), 0, null, null)
        );
        // Seed each presentation listing once, even when the database already has user-created jobs.
        opportunities.saveAll(showcaseListings.stream()
                .filter(listing -> !opportunities.existsByTitleIgnoreCase(listing.getTitle()))
                .toList());
    }

    private Opportunity listing(User employer, String title, String company, String location, String category,
                                String kind, String type, String budget, String description, List<String> skills,
                                LocalDate deadline, int experience, Double latitude, Double longitude) {
        return new Opportunity(employer, new OpportunityRequest(title, company, location, category, kind, type,
                budget, description, skills, deadline, latitude, longitude, experience));
    }
}
