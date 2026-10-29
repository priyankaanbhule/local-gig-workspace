package com.localgig.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(@NotNull Long applicationId, @NotBlank @Size(max = 3000) String body) { }
