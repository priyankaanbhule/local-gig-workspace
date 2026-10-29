package com.localgig.proposal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProposalRequest(
        @NotBlank @Size(max = 3000) String coverLetter,
        @NotNull @DecimalMin(value = "0.01") BigDecimal bidAmount,
        @NotNull @Min(1) Integer estimatedDays
) { }
