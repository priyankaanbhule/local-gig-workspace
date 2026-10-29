package com.localgig.profile;

import com.localgig.user.User;
import com.localgig.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/candidates")
@Transactional(readOnly = true)
public class CandidateSearchController {
    private final ProfileRepository profiles;
    public CandidateSearchController(ProfileRepository profiles) { this.profiles = profiles; }
    @GetMapping("/search")
    public List<ProfileResponse> search(@AuthenticationPrincipal User employer,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String location,
            @RequestParam(defaultValue = "true") boolean available) {
        if (employer == null || (employer.getRole() != UserRole.EMPLOYER && employer.getRole() != UserRole.ADMIN))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Employer access required");
        String term = q.toLowerCase(Locale.ROOT).trim();
        String place = location.toLowerCase(Locale.ROOT).trim();
        return profiles.findAll().stream()
                .filter(p -> !available || p.isAvailable())
                .filter(p -> place.isBlank() || ((p.getCity() == null ? "" : p.getCity()) + " "
                        + (p.getCountry() == null ? "" : p.getCountry())).toLowerCase(Locale.ROOT).contains(place))
                .filter(p -> term.isBlank() || (String.join(" ", p.getSkills()) + " "
                        + String.join(" ", p.getPreferredRoles()) + " "
                        + (p.getHeadline() == null ? "" : p.getHeadline())).toLowerCase(Locale.ROOT).contains(term))
                .map(ProfileResponse::from).toList();
    }
}
