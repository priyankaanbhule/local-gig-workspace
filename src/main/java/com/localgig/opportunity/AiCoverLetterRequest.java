package com.localgig.opportunity;

import jakarta.validation.constraints.NotNull;

public record AiCoverLetterRequest(@NotNull Long opportunityId) { }
