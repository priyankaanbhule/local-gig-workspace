package com.localgig.common.response;

import java.time.Instant;
import java.util.Map;

/** A consistent JSON error shape for LocalGig REST endpoints. */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public ApiErrorResponse {
        fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
    }
}
