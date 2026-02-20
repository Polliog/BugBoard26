package it.unina.bugboard26.dto.request;

import it.unina.bugboard26.model.enums.*;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateIssueRequest(
        @Size(min = 5, max = 200) String title,
        IssueType type,
        @Size(min = 20) String description,
        IssuePriority priority,
        IssueStatus status,
        String assignedToId,
        List<String> labelIds,
        Boolean archived
) {}
