package com.localgig.opportunity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumeMatchRequest(
        @NotBlank(message = "Paste resume text to analyze")
        @Size(max = 20000, message = "Resume text must be 20,000 characters or less")
        String resumeText
) { }