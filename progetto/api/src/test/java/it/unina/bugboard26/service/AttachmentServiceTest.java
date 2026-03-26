package it.unina.bugboard26.service;

import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.enums.IssueStatus;
import it.unina.bugboard26.enums.IssueType;
import it.unina.bugboard26.model.Attachment;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.IssueRepository;
import it.unina.bugboard26.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock private IssueRepository issueRepository;
    @Mock private UserRepository userRepository;
    @Mock private PermissionService permissionService;

    @TempDir Path tempDir;

    private AttachmentService createService() {
        return new AttachmentService(issueRepository, userRepository, permissionService,
                tempDir.toString(), 5242880L,
                List.of("image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"));
    }

    @Test
    @DisplayName("Upload di file con tipo MIME valido salva il file e aggiorna la issue")
    void whenValidFileUploaded_thenAttachmentIsAddedToIssue() throws Exception {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        Issue issue = buildIssue("issue-1", admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(issueRepository.findById("issue-1")).thenReturn(Optional.of(issue));
        when(permissionService.canModifyIssue(admin, issue)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        MockMultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "PDF content".getBytes());

        AttachmentService service = createService();
        service.upload("issue-1", file, admin.getEmail());

        assertEquals(1, issue.getAttachments().size());
        assertEquals("report.pdf", issue.getAttachments().get(0).getOriginalFilename());
        assertEquals("application/pdf", issue.getAttachments().get(0).getContentType());
        verify(issueRepository).save(issue);
    }

    @Test
    @DisplayName("Upload di file con tipo MIME non ammesso lancia errore 400")
    void whenInvalidMimeType_thenBadRequest() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        Issue issue = buildIssue("issue-1", admin);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(issueRepository.findById("issue-1")).thenReturn(Optional.of(issue));
        when(permissionService.canModifyIssue(admin, issue)).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", "application/x-msdownload", "bad".getBytes());

        AttachmentService service = createService();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.upload("issue-1", file, admin.getEmail()));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("EXTERNAL non puo' uploadare allegati")
    void whenExternalUploads_thenAccessDenied() {
        User ext = buildUser("external", GlobalRole.EXTERNAL);
        Issue issue = buildIssue("issue-1", buildUser("owner", GlobalRole.USER));

        when(userRepository.findByEmail(ext.getEmail())).thenReturn(Optional.of(ext));
        when(issueRepository.findById("issue-1")).thenReturn(Optional.of(issue));
        when(permissionService.canModifyIssue(ext, issue)).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "img".getBytes());

        AttachmentService service = createService();
        assertThrows(AccessDeniedException.class,
                () -> service.upload("issue-1", file, ext.getEmail()));
        verify(issueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete rimuove l'allegato dalla issue e dal filesystem")
    void whenDeleteAttachment_thenRemovedFromIssue() throws Exception {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        Issue issue = buildIssue("issue-1", admin);

        Attachment att = new Attachment("uuid-stored.pdf", "report.pdf", "application/pdf", 1024);
        issue.getAttachments().add(att);

        java.nio.file.Files.createFile(tempDir.resolve("uuid-stored.pdf"));

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(issueRepository.findById("issue-1")).thenReturn(Optional.of(issue));
        when(permissionService.canModifyIssue(admin, issue)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        AttachmentService service = createService();
        service.delete("issue-1", "uuid-stored.pdf", admin.getEmail());

        assertTrue(issue.getAttachments().isEmpty());
        verify(issueRepository).save(issue);
    }

    private User buildUser(String name, GlobalRole role) {
        User user = new User();
        user.setId(name + "-id");
        user.setEmail(name + "@test.com");
        user.setPasswordHash("hashed");
        user.setName(name);
        user.setRole(role);
        user.setCreatedAt(Instant.now());
        return user;
    }

    private Issue buildIssue(String id, User creator) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Test Issue");
        issue.setType(IssueType.BUG);
        issue.setDescription("Descrizione di test sufficientemente lunga");
        issue.setStatus(IssueStatus.TODO);
        issue.setCreatedBy(creator);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        issue.setLabels(new ArrayList<>());
        issue.setHistory(new ArrayList<>());
        issue.setAttachments(new ArrayList<>());
        return issue;
    }
}
