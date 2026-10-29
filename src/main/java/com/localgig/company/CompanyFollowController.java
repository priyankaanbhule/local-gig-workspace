package com.localgig.company;

import com.localgig.user.User;
import com.localgig.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Transactional
public class CompanyFollowController {
    private final CompanyFollowRepository follows;
    private final UserRepository users;
    private final CompanyProfileRepository companyProfiles;

    public CompanyFollowController(CompanyFollowRepository follows, UserRepository users,
                                   CompanyProfileRepository companyProfiles) {
        this.follows = follows;
        this.users = users;
        this.companyProfiles = companyProfiles;
    }

    @GetMapping("/following")
    @Transactional(readOnly = true)
    public List<Long> following(@AuthenticationPrincipal User user) {
        return follows.findAllByFollower_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(follow -> follow.getCompany().getId()).toList();
    }

    @PutMapping("/{companyId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@AuthenticationPrincipal User user, @PathVariable Long companyId) {
        if (user.getId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot follow your own company");
        }
        User company = users.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        if (!companyProfiles.findByUserId(companyId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company profile not found");
        }
        if (follows.existsByFollower_IdAndCompany_Id(user.getId(), companyId)) return;
        follows.save(new CompanyFollow(user, company));
    }

    @DeleteMapping("/{companyId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@AuthenticationPrincipal User user, @PathVariable Long companyId) {
        follows.findByFollower_IdAndCompany_Id(user.getId(), companyId).ifPresent(follows::delete);
    }
}
