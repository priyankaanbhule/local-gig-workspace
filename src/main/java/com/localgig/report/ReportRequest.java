package com.localgig.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(@NotBlank String targetType, @NotNull Long targetId,
                            @NotBlank @Size(max = 120) String reason,
                            @Size(max = 1500) String details) { }
