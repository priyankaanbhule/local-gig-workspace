package com.localgig.profile;

import com.localgig.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
@RequestMapping("/api/profiles/me")
public class ProfileController {

    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ProfileResponse getMyProfile(@AuthenticationPrincipal User user) {
        return profileRepository.findByUserId(user.getId())
                .map(ProfileResponse::from)
                .orElseGet(() -> ProfileResponse.emptyFor(user));
    }

    @PutMapping
    public ProfileResponse updateMyProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfileRequest request
    ) {
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> new Profile(user));

        profile.update(request);

        return ProfileResponse.from(profileRepository.save(profile));
    }
}

