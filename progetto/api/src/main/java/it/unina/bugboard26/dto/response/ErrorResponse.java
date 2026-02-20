package it.unina.bugboard26.dto.response;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String message,
        Instant timestamp
) {
    public ErrorResponse(int status, String message) {
        this(status, message, Instant.now());
    }
}
