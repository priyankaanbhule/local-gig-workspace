package com.localgig.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectReviewRequest(@NotNull Long projectId,
                                   @Min(1) @Max(5) int rating,
                                   @Size(max = 2000) String body) { }
