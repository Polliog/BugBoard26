package it.unina.bugboard26.dto.request;

import it.unina.bugboard26.model.enums.GlobalRole;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank @Email String email,

        @NotBlank
        @Size(min = 6, message = "La password deve avere almeno 6 caratteri")
        String password,

        @NotBlank String name,

        @NotNull GlobalRole role
) {}
