package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.UpdateIssueRequest;
import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.enums.IssueStatus;
import it.unina.bugboard26.enums.IssueType;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.IssueRepository;
import it.unina.bugboard26.repository.LabelRepository;
import it.unina.bugboard26.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RF06, RF13 — Test per IssueService.update(String id, UpdateIssueRequest request, String userEmail).
 * Logica di permessi, notifiche e guard di stato.
 */
@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock private IssueRepository issueRepository;
    @Mock private UserRepository userRepository;
    @Mock private LabelRepository labelRepository;
    @Mock private PermissionService permissionService;
    @Mock private NotificationService notificationService;

    @InjectMocks private IssueService issueService;

    /**
     * RF06 — Quando lo stato viene cambiato a RISOLTA da un attore diverso dal creatore,
     * il creatore deve ricevere una notifica con il titolo della issue nel messaggio.
     */
    @Test
    @DisplayName("Cambio stato a RISOLTA notifica il creatore se l'attore è diverso")
    void whenStatusSetToRisolta_andActorIsNotCreator_thenCreatorIsNotified() {
        User creator = buildUser("creator", GlobalRole.USER);
        User admin = buildUser("admin", GlobalRole.ADMIN);
        Issue issue = buildIssue("issue-1", creator, IssueStatus.IN_PROGRESS);

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(issueRepository.findById("issue-1")).thenReturn(Optional.of(issue));
        when(permissionService.canChangeStatus(admin, issue)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        UpdateIssueRequest req = new UpdateIssueRequest(
                null, null, null, null, IssueStatus.RISOLTA, null, null, null, null
        );
        issueService.update("issue-1", req, admin.getEmail());

        verify(notificationService).notifyUser(
                eq(creator.getId()),
                contains("risolta"),
                any(Issue.class)
        );
    }

    /**
     * RF06 — Un utente privo del permesso canModifyIssue non può modificare
     * i campi di una issue: deve ricevere AccessDeniedException e il salvataggio
     * non deve avvenire.
     */
    @Test
    @DisplayName("Utente senza permesso canModifyIssue non può modificare i campi")
    void whenUserLacksModifyPermission_andFieldsAreUpdated_thenAccessDeniedIsThrown() {
        User actor = buildUser("actor", GlobalRole.USER);
        User owner = buildUser("owner", GlobalRole.USER);
        Issue issue = buildIssue("issue-2", owner, IssueStatus.TODO);

        when(userRepository.findByEmail(actor.getEmail())).thenReturn(Optional.of(actor));
        when(issueRepository.findById("issue-2")).thenReturn(Optional.of(issue));
        when(permissionService.canModifyIssue(actor, issue)).thenReturn(false);

        UpdateIssueRequest req = new UpdateIssueRequest(
                "Titolo modificato", null, null, null, null, null, null, null, null
        );

        assertThrows(AccessDeniedException.class,
                () -> issueService.update("issue-2", req, actor.getEmail()));
        verify(issueRepository, never()).save(any());
    }

    /**
     * RF06/RF13 — Una issue soft-deleted non può essere aggiornata:
     * il service deve sollevare ResponseStatusException con stato 404
     * prima di qualsiasi controllo di permesso.
     */
    @Test
    @DisplayName("Aggiornamento di issue soft-deleted lancia 404 NOT_FOUND")
    void whenIssueIsSoftDeleted_thenUpdateThrows404BeforePermissionCheck() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        Issue deletedIssue = buildIssue("issue-3", admin, IssueStatus.TODO);
        deletedIssue.setDeletedAt(Instant.now());

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(issueRepository.findById("issue-3")).thenReturn(Optional.of(deletedIssue));

        UpdateIssueRequest req = new UpdateIssueRequest(
                "Nuovo titolo", null, null, null, null, null, null, null, null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> issueService.update("issue-3", req, admin.getEmail()));
        assertEquals(404, ex.getStatusCode().value());
        verify(permissionService, never()).canModifyIssue(any(), any());
        verify(issueRepository, never()).save(any());
    }

    // --- Helpers ---

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

    private Issue buildIssue(String id, User creator, IssueStatus status) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Test Issue");
        issue.setType(IssueType.BUG);
        issue.setDescription("Descrizione di test sufficientemente lunga");
        issue.setStatus(status);
        issue.setCreatedBy(creator);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        issue.setLabels(new ArrayList<>());
        issue.setHistory(new ArrayList<>());
        return issue;
    }
}
