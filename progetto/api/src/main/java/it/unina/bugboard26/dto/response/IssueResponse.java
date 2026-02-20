package it.unina.bugboard26.dto.response;

import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.enums.*;
import java.time.Instant;
import java.util.List;

/**
 * DTO di risposta per una issue.
 * RF02, RF03 - Visualizzazione issue.
 */
public record IssueResponse(
        String id,
        String title,
        IssueType type,
        String description,
        IssuePriority priority,
        IssueStatus status,
        UserResponse assignedTo,
        UserResponse createdBy,
        Instant createdAt,
        Instant updatedAt,
        String image,
        boolean archived,
        Instant archivedAt,
        UserResponse archivedBy,
        List<String> labels,
        List<HistoryEntryResponse> history
) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getType(),
                issue.getDescription(),
                issue.getPriority(),
                issue.getStatus(),
                issue.getAssignedTo() != null ? UserResponse.from(issue.getAssignedTo()) : null,
                UserResponse.from(issue.getCreatedBy()),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getImage(),
                issue.isArchived(),
                issue.getArchivedAt(),
                issue.getArchivedBy() != null ? UserResponse.from(issue.getArchivedBy()) : null,
                issue.getLabels().stream().map(l -> l.getName()).toList(),
                issue.getHistory().stream().map(HistoryEntryResponse::from).toList()
        );
    }
}
