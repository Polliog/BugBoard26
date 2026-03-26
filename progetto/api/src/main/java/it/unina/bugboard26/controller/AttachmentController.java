package it.unina.bugboard26.controller;

import it.unina.bugboard26.dto.response.AttachmentResponse;
import it.unina.bugboard26.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/issues/{issueId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> upload(@PathVariable String issueId,
                                                      @RequestParam("file") MultipartFile file,
                                                      Authentication auth) throws IOException {
        AttachmentResponse response = attachmentService.upload(issueId, file, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{storedFilename}")
    public ResponseEntity<Resource> download(@PathVariable String issueId,
                                             @PathVariable String storedFilename) throws IOException {
        Resource resource = attachmentService.download(issueId, storedFilename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{storedFilename}")
    public ResponseEntity<Void> delete(@PathVariable String issueId,
                                        @PathVariable String storedFilename,
                                        Authentication auth) throws IOException {
        attachmentService.delete(issueId, storedFilename, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
