package com.localgig.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyVerificationReviewRequest(@NotBlank String status, @Size(max = 1000) String note) { }
