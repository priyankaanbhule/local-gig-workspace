package com.localgig.opportunity;

import com.localgig.profile.Profile;
import com.localgig.profile.ProfileRepository;
import com.localgig.security.RoleGuard;
import com.localgig.user.User;
import com.localgig.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/opportunities")
@Transactional(readOnly = true)
public class ResumeMatchController {
    private static final Pattern WORDS = Pattern.compile("[^\\p{L}\\p{N}+#.]+");
    private static final Set<String> STOP_WORDS = Set.of("and", "the", "for", "with", "from", "job", "role", "work", "years", "year", "required", "experience", "developer", "engineer");
    private final OpportunityRepository opportunities;
    private final ProfileRepository profiles;
    private final RoleGuard roleGuard;

    public ResumeMatchController(OpportunityRepository opportunities, ProfileRepository profiles, RoleGuard roleGuard) {
        this.opportunities = opportunities;
        this.profiles = profiles;
        this.roleGuard = roleGuard;
    }

    @PostMapping("/{id}/match")
    public ResumeMatchResponse analyze(@AuthenticationPrincipal User user, @PathVariable Long id,
                                       @Valid @RequestBody ResumeMatchRequest request) {
        roleGuard.requireAny(user, UserRole.JOB_SEEKER, UserRole.FREELANCER, UserRole.ADMIN);
        Opportunity opportunity = opportunities.findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
        Profile profile = profiles.findByUserId(user.getId()).orElse(null);
        String source = request.resumeText();
        if (profile != null) {
            source += " " + String.join(" ", profile.getSkills()) + " " + safe(profile.getSummary()) + " "
                    + String.join(" ", profile.getExperience()) + " " + String.join(" ", profile.getProjects());
        }
        Set<String> resumeWords = tokenize(source);
        List<String> requiredSkills = opportunity.getSkills().stream().filter(s -> !s.isBlank()).distinct().toList();
        List<String> matchedSkills = requiredSkills.stream().filter(s -> containsTerm(resumeWords, s)).toList();
        List<String> missingSkills = requiredSkills.stream().filter(s -> !containsTerm(resumeWords, s)).toList();
        Set<String> requiredKeywords = tokenize(opportunity.getTitle() + " " + opportunity.getCategory() + " " + opportunity.getDescription());
        List<String> matchedKeywords = requiredKeywords.stream().filter(resumeWords::contains).sorted().limit(30).toList();
        List<String> missingKeywords = requiredKeywords.stream().filter(w -> !resumeWords.contains(w)).sorted().limit(30).toList();
        int skillScore = requiredSkills.isEmpty() ? 0 : (int)Math.round(75.0 * matchedSkills.size() / requiredSkills.size());
        int keywordScore = requiredKeywords.isEmpty() ? 0 : (int)Math.round(25.0 * matchedKeywords.size() / Math.min(requiredKeywords.size(), 30));
        int score = Math.min(100, skillScore + keywordScore);
        return new ResumeMatchResponse(opportunity.getId(), opportunity.getTitle(), score, matchedSkills,
                missingSkills, matchedKeywords, missingKeywords,
                missingSkills.isEmpty() ? "Your resume text includes all listed skills." : "Consider adding relevant skills only if you have them.");
    }

    private Set<String> tokenize(String text) {
        return WORDS.splitAsStream(text.toLowerCase(Locale.ROOT)).map(String::trim)
                .filter(word -> word.length() > 1 && !STOP_WORDS.contains(word))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    private boolean containsTerm(Set<String> words, String term) {
        Set<String> target = tokenize(term);
        return !target.isEmpty() && target.stream().allMatch(words::contains);
    }
    private String safe(String value) { return value == null ? "" : value; }
    public record ResumeMatchResponse(Long opportunityId, String title, int matchPercent,
            List<String> matchedSkills, List<String> missingSkills,
            List<String> matchedKeywords, List<String> missingKeywords, String guidance) { }
}