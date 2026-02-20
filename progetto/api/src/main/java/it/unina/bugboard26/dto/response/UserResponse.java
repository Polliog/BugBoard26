package it.unina.bugboard26.dto.response;

import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.GlobalRole;
import java.time.Instant;

public record UserResponse(
        String id,
        String email,
        String name,
        GlobalRole role,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
