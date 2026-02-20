package it.unina.bugboard26.dto.response;

import it.unina.bugboard26.model.Comment;
import java.time.Instant;

public record CommentResponse(
        String id,
        String issueId,
        UserResponse author,
        String content,
        String image,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getIssue().getId(),
                UserResponse.from(c.getUser()),
                c.getContent(),
                c.getImage(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
