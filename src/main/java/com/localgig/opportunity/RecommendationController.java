package com.localgig.opportunity;

import com.localgig.profile.Profile;
import com.localgig.profile.ProfileRepository;
import com.localgig.security.RoleGuard;
import com.localgig.user.User;
import com.localgig.user.UserRole;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/opportunities")
@Transactional(readOnly = true)
public class RecommendationController {
    private final OpportunityRepository opportunities;
    private final ProfileRepository profiles;
    private final RoleGuard roleGuard;

    public RecommendationController(OpportunityRepository opportunities, ProfileRepository profiles, RoleGuard roleGuard) {
        this.opportunities = opportunities;
        this.profiles = profiles;
        this.roleGuard = roleGuard;
    }

    @GetMapping("/recommended")
    public List<Recommendation> recommended(@AuthenticationPrincipal User user) {
        roleGuard.requireAny(user, UserRole.JOB_SEEKER, UserRole.FREELANCER, UserRole.ADMIN);
        Profile profile = profiles.findByUserId(user.getId()).orElse(null);
        Set<String> skills = profile == null ? Set.of() : normalize(profile.getSkills());
        List<String> roles = profile == null ? List.of() : profile.getPreferredRoles();
        List<String> locations = profile == null ? List.of() : profile.getPreferredLocations();
        int experienceYears = profile == null ? 0 : profile.getExperience().size();
        return opportunities.findAllByStatusOrderByCreatedAtDesc("ACTIVE").stream()
                .map(item -> score(item, skills, roles, locations, experienceYears))
                .sorted(Comparator.comparingInt(Recommendation::matchPercent).reversed()
                        .thenComparing(r -> r.opportunity().createdAt(), Comparator.reverseOrder()))
                .limit(20).toList();
    }

    private Recommendation score(Opportunity item, Set<String> skills, List<String> roles,
                                 List<String> locations, int experienceYears) {
        List<String> matched = item.getSkills().stream().filter(s -> skills.contains(key(s))).distinct().toList();
        List<String> missing = item.getSkills().stream().filter(s -> !skills.contains(key(s))).distinct().toList();
        int skillScore = item.getSkills().isEmpty() ? 0 : (int)Math.round(70.0 * matched.size() / item.getSkills().size());
        boolean roleMatch = roles.stream().anyMatch(r -> contains(item.getTitle(), r) || contains(item.getCategory(), r));
        boolean locationMatch = locations.stream().anyMatch(l -> contains(item.getLocation(), l));
        boolean experienceMatch = item.getRequiredExperienceYears() == 0 || experienceYears >= item.getRequiredExperienceYears();
        int score = Math.min(100, skillScore + (roleMatch ? 15 : 0) + (locationMatch ? 10 : 0) + (experienceMatch ? 5 : 0));
        return new Recommendation(OpportunityResponse.from(item), score, matched, missing,
                "Based on your profile skills, preferred roles, location, and experience.");
    }
    private Set<String> normalize(List<String> values) { return values.stream().map(this::key).collect(Collectors.toSet()); }
    private String key(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
    private boolean contains(String text, String fragment) { return text != null && fragment != null && text.toLowerCase(Locale.ROOT).contains(fragment.toLowerCase(Locale.ROOT)); }

    public record Recommendation(OpportunityResponse opportunity, int matchPercent, List<String> matchedSkills,
                                 List<String> missingSkills, String explanation) { }
}