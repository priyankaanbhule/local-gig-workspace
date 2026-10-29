package com.localgig.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyVerificationRequest(@NotBlank @Size(max = 160) String companyName,
                                         @Size(max = 100) String registrationNumber,
                                         @Size(max = 500) String website,
                                         @Size(max = 500) String evidenceUrl) { }
