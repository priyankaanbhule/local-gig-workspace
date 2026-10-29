package com.localgig.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportResolutionRequest(@NotBlank String status, @Size(max = 1000) String note) { }
