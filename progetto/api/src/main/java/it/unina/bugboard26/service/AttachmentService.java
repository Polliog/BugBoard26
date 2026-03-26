package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.response.AttachmentResponse;
import it.unina.bugboard26.model.Attachment;
import it.unina.bugboard26.model.HistoryEntry;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.IssueRepository;
import it.unina.bugboard26.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional(readOnly = true)
public class AttachmentService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final Path uploadDir;
    private final long maxSize;
    private final List<String> allowedTypes;

    public AttachmentService(IssueRepository issueRepository,
                             UserRepository userRepository,
                             PermissionService permissionService,
                             @Value("${bugboard.attachments.dir:uploads}") String uploadDir,
                             @Value("${bugboard.attachments.max-size:5242880}") long maxSize,
                             @Value("${bugboard.attachments.allowed-types:image/jpeg,image/png,image/gif,image/webp,application/pdf}") List<String> allowedTypes) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
        this.uploadDir = Paths.get(uploadDir);
        this.maxSize = maxSize;
        this.allowedTypes = allowedTypes;
    }

    private User resolveUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Utente non trovato"));
    }

    private Issue resolveIssue(String issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Issue non trovata"));
        if (issue.getDeletedAt() != null) {
            throw new ResponseStatusException(NOT_FOUND, "Issue eliminata");
        }
        return issue;
    }

    @Transactional
    public AttachmentResponse upload(String issueId, MultipartFile file, String userEmail) throws IOException {
        User user = resolveUser(userEmail);
        Issue issue = resolveIssue(issueId);

        if (!permissionService.canModifyIssue(user, issue)) {
            throw new AccessDeniedException("Non hai i permessi per aggiungere allegati");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Tipo file non ammesso. Tipi validi: " + String.join(", ", allowedTypes));
        }
        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(BAD_REQUEST, "File troppo grande. Massimo 5MB.");
        }
        if (issue.getAttachments().size() >= 10) {
            throw new ResponseStatusException(BAD_REQUEST, "Massimo 10 allegati per issue.");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String storedFilename = UUID.randomUUID() + extension;

        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(storedFilename), StandardCopyOption.REPLACE_EXISTING);

        Attachment attachment = new Attachment(storedFilename, originalFilename, contentType, file.getSize());
        issue.getAttachments().add(attachment);
        issue.getHistory().add(new HistoryEntry(issue, user, "Allegato aggiunto: " + originalFilename));
        issueRepository.save(issue);

        return AttachmentResponse.from(attachment);
    }

    public Resource download(String issueId, String storedFilename) throws IOException {
        Issue issue = resolveIssue(issueId);
        issue.getAttachments().stream()
                .filter(a -> a.getStoredFilename().equals(storedFilename))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Allegato non trovato"));

        Path filePath = uploadDir.resolve(storedFilename);
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            throw new ResponseStatusException(NOT_FOUND, "File non trovato su disco");
        }
        return resource;
    }

    @Transactional
    public void delete(String issueId, String storedFilename, String userEmail) throws IOException {
        User user = resolveUser(userEmail);
        Issue issue = resolveIssue(issueId);

        if (!permissionService.canModifyIssue(user, issue)) {
            throw new AccessDeniedException("Non hai i permessi per rimuovere allegati");
        }

        Attachment attachment = issue.getAttachments().stream()
                .filter(a -> a.getStoredFilename().equals(storedFilename))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Allegato non trovato"));

        issue.getAttachments().remove(attachment);
        issue.getHistory().add(new HistoryEntry(issue, user, "Allegato rimosso: " + attachment.getOriginalFilename()));

        Path filePath = uploadDir.resolve(storedFilename);
        Files.deleteIfExists(filePath);
        issueRepository.save(issue);
    }
}
