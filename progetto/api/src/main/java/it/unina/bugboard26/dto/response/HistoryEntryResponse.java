package it.unina.bugboard26.dto.response;

import it.unina.bugboard26.model.HistoryEntry;
import java.time.Instant;

/**
 * DTO di risposta per una voce di storico.
 */
public record HistoryEntryResponse(
        String id,
        UserResponse performedBy,
        String action,
        Instant timestamp
) {
    public static HistoryEntryResponse from(HistoryEntry e) {
        return new HistoryEntryResponse(
                e.getId(),
                UserResponse.from(e.getUser()),
                e.getAction(),
                e.getTimestamp()
        );
    }
}
