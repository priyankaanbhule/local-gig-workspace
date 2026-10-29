package com.localgig.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectMessageRequest(@NotBlank @Size(max = 3000) String body) { }
