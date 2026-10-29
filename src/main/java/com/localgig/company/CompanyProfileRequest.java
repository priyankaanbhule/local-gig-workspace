package com.localgig.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyProfileRequest(
        @NotBlank @Size(max = 160) String companyName,
        @Size(max = 180) String tagline,
        @Size(max = 3000) String description,
        @Size(max = 500) String website,
        @Size(max = 500) String linkedinUrl,
        @Size(max = 120) String industry,
        @Size(max = 120) String headquarters,
        @Size(max = 500) String logoUrl,
        @Size(max = 50) String employeeCount
) { }
