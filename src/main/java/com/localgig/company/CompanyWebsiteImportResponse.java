package com.localgig.company;

public record CompanyWebsiteImportResponse(
        String companyName,
        String tagline,
        String description,
        String website,
        String linkedinUrl,
        String industry,
        String headquarters,
        String logoUrl,
        String employeeCount
) { }
