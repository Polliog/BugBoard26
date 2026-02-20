package it.unina.bugboard26.dto.request;

import it.unina.bugboard26.model.enums.GlobalRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email String email,
        String name,
        @Size(min = 6, message = "La password deve avere almeno 6 caratteri") String password,
        GlobalRole role
) {}
