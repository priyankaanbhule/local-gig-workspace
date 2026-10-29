package com.localgig.admin;

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
import java.util.Locale;

@RestController
@RequestMapping("/api/categories")
@Transactional
public class ManagedCategoryController {
    private final ManagedCategoryRepository categories;
    private final RoleGuard roleGuard;

    public ManagedCategoryController(ManagedCategoryRepository categories, RoleGuard roleGuard) {
        this.categories = categories;
        this.roleGuard = roleGuard;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ManagedCategoryResponse> list(@RequestParam(defaultValue = "JOB") String scope) {
        return categories.findAllByScopeAndActiveTrueOrderByNameAsc(normalizeScope(scope)).stream()
                .map(ManagedCategoryResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagedCategoryResponse create(@AuthenticationPrincipal User admin,
                                           @Valid @RequestBody ManagedCategoryRequest request) {
        roleGuard.require(admin, UserRole.ADMIN);
        String scope = normalizeScope(request.scope());
        String name = request.name().trim();
        if (categories.existsByScopeAndNormalizedName(scope, name.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That category already exists");
        }
        return ManagedCategoryResponse.from(categories.save(new ManagedCategory(scope, name)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@AuthenticationPrincipal User admin, @PathVariable Long id) {
        roleGuard.require(admin, UserRole.ADMIN);
        ManagedCategory category = categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.deactivate();
    }

    private String normalizeScope(String value) {
        String scope = value.trim().toUpperCase(Locale.ROOT);
        if (!scope.equals("JOB") && !scope.equals("COURSE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scope must be JOB or COURSE");
        }
        return scope;
    }
}
