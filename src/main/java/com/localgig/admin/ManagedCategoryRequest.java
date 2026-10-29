package com.localgig.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ManagedCategoryRequest(
        @NotBlank @Pattern(regexp = "(?i)JOB|COURSE", message = "Scope must be JOB or COURSE") String scope,
        @NotBlank @Size(max = 80) String name
) { }
