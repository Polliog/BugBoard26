package it.unina.bugboard26.dto.request;

import it.unina.bugboard26.model.enums.*;
import jakarta.validation.constraints.*;
import java.util.List;

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
