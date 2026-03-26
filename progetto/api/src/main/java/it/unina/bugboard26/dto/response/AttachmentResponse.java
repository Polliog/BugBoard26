package it.unina.bugboard26.dto.response;

import it.unina.bugboard26.model.Attachment;
import java.time.Instant;

public record AttachmentResponse(
        String storedFilename,
        String originalFilename,
        String contentType,
        long fileSize,
        Instant uploadedAt
) {
    public static AttachmentResponse from(Attachment a) {
        return new AttachmentResponse(
                a.getStoredFilename(), a.getOriginalFilename(),
                a.getContentType(), a.getFileSize(), a.getUploadedAt()
        );
    }
}
