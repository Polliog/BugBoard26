package it.unina.bugboard26.dto.request;

import jakarta.validation.constraints.*;

public record UpdateCommentRequest(
        @NotBlank
        @Size(min = 3, max = 1000)
        String content,
        String image
) {}
