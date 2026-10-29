package com.localgig.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyWebsiteImportRequest(
        @NotBlank(message = "Enter the company's public website")
        @Size(max = 500, message = "Website URL must be 500 characters or less")
        String website
) { }
