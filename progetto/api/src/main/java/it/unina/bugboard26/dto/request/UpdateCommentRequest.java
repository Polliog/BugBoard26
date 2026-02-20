package it.unina.bugboard26.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO per l'aggiornamento di un commento.
 */
public record UpdateCommentRequest(
        @NotBlank
        @Size(min = 3, max = 1000)
        String content
) {}
