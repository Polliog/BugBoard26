package it.unina.bugboard26.dto.request;

import it.unina.bugboard26.model.enums.*;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO per la richiesta di export.
 * RF08 - Export CSV/PDF.
 * Accetta gli stessi filtri di GET /api/issues (RF03).
 */
public record ExportRequest(
        @NotNull
        @Pattern(regexp = "csv|pdf")
        String format,

        List<IssueType> types,
        List<IssueStatus> statuses,
        List<IssuePriority> priorities,
        String assignedToId,
        String search,
        boolean includeArchived
) {}
