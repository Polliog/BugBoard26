package it.unina.bugboard26.dto.response;

import it.unina.bugboard26.model.Notification;
import java.time.Instant;

/**
 * DTO di risposta per una notifica.
 * RF06 - Notifiche cambio stato.
 */
public record NotificationResponse(
        String id,
        String message,
        String issueId,
        String issueTitle,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getMessage(),
                n.getIssue() != null ? n.getIssue().getId() : null,
                n.getIssue() != null ? n.getIssue().getTitle() : null,
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
