package it.unina.bugboard26.dto.request;

import it.unina.bugboard26.model.enums.IssuePriority;
import it.unina.bugboard26.model.enums.IssueType;
import jakarta.validation.constraints.*;

public record CreateIssueRequest(
        @NotBlank
        @Size(min = 5, max = 200)
        String title,

        @NotNull IssueType type,

        @NotBlank
        @Size(min = 20)
        String description,

        IssuePriority priority,

        String assignedToId,

        String image
) {}
