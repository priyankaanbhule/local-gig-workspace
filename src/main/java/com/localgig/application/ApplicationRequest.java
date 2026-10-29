package com.localgig.application;

import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @Size(max = 3000) String coverLetter,
        @Size(max = 500) String resumeUrl
) { }
