package com.localgig.opportunity;

import com.localgig.alert.JobAlertService;
import com.localgig.application.ApplicationRepository;
import com.localgig.proposal.ProposalRepository;
import com.localgig.security.RoleGuard;
import com.localgig.user.User;
import com.localgig.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
@Transactional
public class OpportunityController {
    private final OpportunityRepository opportunities;
    private final JobAlertService jobAlerts;
    private final ApplicationRepository applications;
    private final ProposalRepository proposals;
    private final RoleGuard roleGuard;

    public OpportunityController(OpportunityRepository opportunities, JobAlertService jobAlerts,
                                 ApplicationRepository applications, ProposalRepository proposals, RoleGuard roleGuard) {
        this.opportunities = opportunities;
        this.jobAlerts = jobAlerts;
        this.applications = applications;
        this.proposals = proposals;
        this.roleGuard = roleGuard;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<OpportunityResponse> browse() {
        return opportunities.findAllByStatusOrderByCreatedAtDesc("ACTIVE")
                .stream().map(OpportunityResponse::from).toList();
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public Page<OpportunityResponse> search(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String location,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String kind,
            @RequestParam(defaultValue = "") String type,
            @RequestParam(required = false) Double minBudget,
            @RequestParam(required = false) Double maxBudget,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or page size");
        String query = q.toLowerCase().trim();
        var results = opportunities.findAllByStatusOrderByCreatedAtDesc("ACTIVE").stream()
                .filter(o -> query.isBlank() || (o.getTitle() + " " + o.getCompany() + " " + o.getCategory() + " "
                        + o.getDescription() + " " + String.join(" ", o.getSkills())).toLowerCase().contains(query))
                .filter(o -> location.isBlank() || o.getLocation().toLowerCase().contains(location.toLowerCase().trim()))
                .filter(o -> category.isBlank() || o.getCategory().equalsIgnoreCase(category.trim()))
                .filter(o -> kind.isBlank() || o.getKind().equalsIgnoreCase(kind.trim()))
                .filter(o -> type.isBlank() || o.getType().equalsIgnoreCase(type.trim()))
                .filter(o -> minBudget == null || numericBudget(o.getBudget()) >= minBudget)
                .filter(o -> maxBudget == null || numericBudget(o.getBudget()) <= maxBudget)
                .filter(o -> minExperience == null || o.getRequiredExperienceYears() >= minExperience)
                .filter(o -> maxExperience == null || o.getRequiredExperienceYears() <= maxExperience)
                .toList();
        if (minBudget != null && minBudget < 0 || maxBudget != null && maxBudget < 0
                || minExperience != null && minExperience < 0 || maxExperience != null && maxExperience < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filter values cannot be negative");
        }
        if (minBudget != null && maxBudget != null && minBudget > maxBudget) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum budget cannot exceed maximum budget");
        }
        if (sort.equalsIgnoreCase("salary")) results = results.stream().sorted((a, b) -> Double.compare(numericBudget(b.getBudget()), numericBudget(a.getBudget()))).toList();
        else if (sort.equalsIgnoreCase("relevance") && !query.isBlank()) results = results.stream()
                .sorted((a, b) -> Integer.compare(matches(b, query), matches(a, query))).toList();
        int from = (int) Math.min((long) page * size, results.size());
        int to = Math.min(from + size, results.size());
        return new PageImpl<>(results.subList(from, to).stream().map(OpportunityResponse::from).toList(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), results.size());
    }

    @GetMapping("/nearby")
    @Transactional(readOnly = true)
    public List<NearbyOpportunityResponse> nearby(@RequestParam double latitude, @RequestParam double longitude,
                                                  @RequestParam(defaultValue = "25") double radiusKm) {
        if (radiusKm <= 0 || radiusKm > 500) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Radius must be between 0 and 500 km");
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude or longitude is outside its valid range");
        }
        return opportunities.findAllByStatusOrderByCreatedAtDesc("ACTIVE").stream()
                .filter(o -> o.getLatitude() != null && o.getLongitude() != null)
                .map(o -> new NearbyOpportunityResponse(OpportunityResponse.from(o), distance(latitude, longitude, o.getLatitude(), o.getLongitude())))
                .filter(o -> o.distanceKm() <= radiusKm)
                .sorted(java.util.Comparator.comparingDouble(NearbyOpportunityResponse::distanceKm)).toList();
    }

    @GetMapping("/{id}/similar")
    @Transactional(readOnly = true)
    public List<OpportunityResponse> similar(@PathVariable Long id) {
        Opportunity source = opportunities.findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
        return opportunities.findAllByStatusOrderByCreatedAtDesc("ACTIVE").stream()
                .filter(o -> !o.getId().equals(id) && (o.getCategory().equalsIgnoreCase(source.getCategory()) || o.getKind().equalsIgnoreCase(source.getKind())))
                .limit(5).map(OpportunityResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public OpportunityResponse get(@PathVariable Long id) {
        return OpportunityResponse.from(opportunities.findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found")));
    }

    @GetMapping("/mine")
    public List<OpportunityResponse> mine(@AuthenticationPrincipal User user) {
        requireEmployer(user);
        return opportunities.findAllByEmployerIdOrderByCreatedAtDesc(user.getId())
                .stream().map(OpportunityResponse::from).toList();
    }

    @GetMapping("/mine/stats")
    @Transactional(readOnly = true)
    public EmployerStats mineStats(@AuthenticationPrincipal User user) {
        requireEmployer(user);
        List<Opportunity> mine = opportunities.findAllByEmployerIdOrderByCreatedAtDesc(user.getId());
        long active = mine.stream().filter(item -> item.getStatus().equals("ACTIVE")).count();
        long applicationCount = mine.stream().mapToLong(item -> applications.findAllForOpportunity(item.getId()).size()).sum();
        long proposalCount = mine.stream().mapToLong(item -> proposals.findAllByOpportunityIdOrderByCreatedAtDesc(item.getId()).size()).sum();
        return new EmployerStats(mine.size(), active, applicationCount, proposalCount);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse create(@AuthenticationPrincipal User user,
                                     @Valid @RequestBody OpportunityRequest request) {
        requireEmployer(user);
        Opportunity saved = opportunities.save(new Opportunity(user, request));
        jobAlerts.notifyMatching(saved);
        return OpportunityResponse.from(saved);
    }

    @PutMapping("/{id}")
    public OpportunityResponse update(@AuthenticationPrincipal User user, @PathVariable Long id,
                                     @Valid @RequestBody OpportunityRequest request) {
        Opportunity opportunity = ownedOpportunity(user, id);
        opportunity.update(request);
        return OpportunityResponse.from(opportunity);
    }

    @PatchMapping("/{id}/status")
    public OpportunityResponse setStatus(@AuthenticationPrincipal User user, @PathVariable Long id,
                                        @RequestParam boolean active) {
        Opportunity opportunity = ownedOpportunity(user, id);
        if (active && opportunity.getDeadline() != null
                && opportunity.getDeadline().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An expired opportunity cannot be reopened");
        }
        if (active) opportunity.reopen(); else opportunity.close();
        return OpportunityResponse.from(opportunity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        opportunities.delete(ownedOpportunity(user, id));
    }

    private Opportunity ownedOpportunity(User user, Long id) {
        requireEmployer(user);
        Opportunity item = opportunities.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
        if (!item.getEmployer().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this opportunity");
        }
        return item;
    }

    private void requireEmployer(User user) {
        roleGuard.requireAny(user, UserRole.EMPLOYER, UserRole.ADMIN);
    }

    private int matches(Opportunity o, String q) {
        String title = o.getTitle().toLowerCase();
        String skills = String.join(" ", o.getSkills()).toLowerCase();
        return (title.contains(q) ? 3 : 0) + (skills.contains(q) ? 2 : 0)
                + (o.getCompany().toLowerCase().contains(q) ? 1 : 0);
    }
    private double numericBudget(String budget) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[0-9]+(?:\\.[0-9]+)?").matcher(budget.replace(",", ""));
        if (!matcher.find()) return 0;
        double amount = Double.parseDouble(matcher.group());
        String lower = budget.toLowerCase();
        if (lower.contains("lpa") || lower.contains("per annum") || lower.contains("/year")) amount *= 100000;
        return amount;
    }
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double earth = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * earth * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
    public record NearbyOpportunityResponse(OpportunityResponse opportunity, double distanceKm) { }
    public record EmployerStats(long totalListings, long activeListings, long applications, long proposals) { }
}
