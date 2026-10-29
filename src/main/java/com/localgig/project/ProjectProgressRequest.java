package com.localgig.project;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProjectProgressRequest(@Min(0) @Max(100) int progressPercent,
                                     @Size(max = 2000) String progressNote,
                                     @Size(max = 500) String deliverableUrl) { }
