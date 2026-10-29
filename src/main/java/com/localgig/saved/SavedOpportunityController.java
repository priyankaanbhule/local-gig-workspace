package com.localgig.saved;

import com.localgig.opportunity.OpportunityRepository;
import com.localgig.opportunity.OpportunityResponse;
import com.localgig.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/saved")
@Transactional
public class SavedOpportunityController {
    private final SavedOpportunityRepository saved;
    private final OpportunityRepository opportunities;
    public SavedOpportunityController(SavedOpportunityRepository saved, OpportunityRepository opportunities) {
        this.saved = saved; this.opportunities = opportunities;
    }

    @GetMapping
    public List<OpportunityResponse> list(@AuthenticationPrincipal User user) {
        return saved.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(SavedOpportunity::getOpportunity).map(OpportunityResponse::from).toList();
    }

    @PutMapping("/{opportunityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void save(@AuthenticationPrincipal User user, @PathVariable Long opportunityId) {
        if (saved.findByUserIdAndOpportunityId(user.getId(), opportunityId).isPresent()) return;
        var opportunity = opportunities.findByIdAndStatus(opportunityId, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
        saved.save(new SavedOpportunity(user, opportunity));
    }

    @DeleteMapping("/{opportunityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal User user, @PathVariable Long opportunityId) {
        saved.findByUserIdAndOpportunityId(user.getId(), opportunityId).ifPresent(saved::delete);
    }
}
