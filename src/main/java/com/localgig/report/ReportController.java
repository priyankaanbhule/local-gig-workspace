package com.localgig.report;

import com.localgig.opportunity.OpportunityRepository;
import com.localgig.user.User;
import com.localgig.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reports")
@Transactional
public class ReportController {
    private final ReportRepository reports;
    private final OpportunityRepository opportunities;
    private final UserRepository users;
    public ReportController(ReportRepository reports, OpportunityRepository opportunities, UserRepository users) {
        this.reports = reports; this.opportunities = opportunities; this.users = users;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse create(@AuthenticationPrincipal User reporter, @Valid @RequestBody ReportRequest request) {
        String type = request.targetType().trim().toUpperCase();
        if (type.equals("LISTING") && !opportunities.existsById(request.targetId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        if (type.equals("USER") && !users.existsById(request.targetId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        if (!type.equals("LISTING") && !type.equals("USER"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report target must be LISTING or USER");
        if (type.equals("USER") && reporter.getId().equals(request.targetId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot report your own account");
        Report report = reports.save(new Report(reporter, type, request.targetId(), request.reason().trim(),
                request.details() == null ? "" : request.details().trim()));
        return ReportResponse.from(report);
    }
}
