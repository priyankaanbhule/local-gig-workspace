package com.localgig.proposal;

import jakarta.validation.constraints.NotNull;

public record ProposalStatusRequest(@NotNull ProposalStatus status) { }
