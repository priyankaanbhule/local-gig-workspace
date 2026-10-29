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

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Transactional(readOnly = true)
public class AiCoverLetterController {
    private final OpportunityRepository opportunities;
    private final ProfileRepository profiles;
    private final OpenAiCoverLetterService ai;
    private final RoleGuard roleGuard;

    public AiCoverLetterController(OpportunityRepository opportunities, ProfileRepository profiles,
                                   OpenAiCoverLetterService ai, RoleGuard roleGuard) {
        this.opportunities = opportunities;
        this.profiles = profiles;
        this.ai = ai;
        this.roleGuard = roleGuard;
    }

    @PostMapping("/cover-letter")
    public AiCoverLetterResponse coverLetter(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody AiCoverLetterRequest request) {
        roleGuard.requireAny(user, UserRole.JOB_SEEKER, UserRole.FREELANCER);
        Opportunity opportunity = opportunities.findByIdAndStatus(request.opportunityId(), "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active opportunity not found"));
        Profile profile = profiles.findByUserId(user.getId()).orElse(null);
        String summary = profile == null ? "" : String.join("\n", List.of(
                safe(profile.getHeadline()), safe(profile.getSummary()),
                "Experience: " + String.join("; ", profile.getExperience()),
                "Education: " + String.join("; ", profile.getEducation()),
                "Projects: " + String.join("; ", profile.getProjects())));
        List<String> skills = profile == null ? List.of() : profile.getSkills();
        return new AiCoverLetterResponse(ai.draft(opportunity.getTitle(), opportunity.getCompany(),
                opportunity.getDescription(), summary, skills));
    }

    private String safe(String value) { return value == null ? "" : value; }
}
