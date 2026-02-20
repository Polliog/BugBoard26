package it.unina.bugboard26.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "L'email e' obbligatoria")
        @Email(message = "Formato email non valido")
        String email,

        @NotBlank(message = "La password e' obbligatoria")
        String password
) {}
