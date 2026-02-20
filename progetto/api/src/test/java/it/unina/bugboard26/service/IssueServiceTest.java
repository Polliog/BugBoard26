package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.CreateIssueRequest;
import it.unina.bugboard26.dto.request.UpdateIssueRequest;
import it.unina.bugboard26.dto.response.IssueResponse;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.*;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test per IssueService.
 * RF02, RF06, RF13 - Creazione, cambio stato, archiviazione.
 */
@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private IssueService issueService;

    /**
     * RF02 - ADMIN puo creare una issue.
     */
    @Test
    @DisplayName("ADMIN crea issue con successo")
    void adminCreatesIssueSuccessfully() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        CreateIssueRequest request = new CreateIssueRequest(
                "Bug critico nel login",
                IssueType.BUG,
                "Descrizione dettagliata del bug nel sistema di login",
                IssuePriority.CRITICA,
                null,
                null
        );

        when(permissionService.canCreateIssue(admin)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue saved = invocation.getArgument(0);
            saved.setId("new-issue-id");
            saved.setCreatedAt(Instant.now());
            saved.setUpdatedAt(Instant.now());
            return saved;
        });

        IssueResponse response = issueService.create(request, admin);

        assertNotNull(response);
        assertEquals("Bug critico nel login", response.title());
        assertEquals(IssueType.BUG, response.type());
        assertEquals(IssueStatus.TODO, response.status());
        verify(issueRepository).save(any(Issue.class));
    }

    /**
     * RF02 - EXTERNAL non puo creare issue.
     */
    @Test
    @DisplayName("EXTERNAL non puo creare issue")
    void externalCannotCreateIssue() {
        User external = buildUser("external", GlobalRole.EXTERNAL);
        CreateIssueRequest request = new CreateIssueRequest(
                "Tentativo di creazione",
                IssueType.FEATURE,
                "Descrizione sufficientemente lunga per passare la validazione",
                null,
                null,
                null
        );

        when(permissionService.canCreateIssue(external)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> issueService.create(request, external));
        verify(issueRepository, never()).save(any());
    }

    /**
     * RF06 - Cambio stato a RISOLTA notifica il creatore.
     */
    @Test
    @DisplayName("Cambio stato a RISOLTA notifica il creatore")
    void whenStatusSetToRisolta_thenNotifyCreator() {
        User creator = buildUser("creator", GlobalRole.USER);
        User assignee = buildUser("assignee", GlobalRole.USER);
        Issue issue = buildIssue(creator, assignee, IssueStatus.IN_PROGRESS);

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(permissionService.canChangeStatus(assignee, issue)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        UpdateIssueRequest req = new UpdateIssueRequest(
                null, null, null, null, IssueStatus.RISOLTA, null, null, null
        );
        issueService.update(issue.getId(), req, assignee);

        verify(notificationService).notifyUser(eq(creator.getId()), contains("risolta"), eq(issue));
    }

    /**
     * RF06 - Cambio stato non notifica se l'utente e lo stesso creatore.
     */
    @Test
    @DisplayName("Cambio stato dal creatore non genera notifica a se stesso")
    void whenCreatorChangesStatus_thenNoSelfNotification() {
        User creator = buildUser("creator", GlobalRole.USER);
        Issue issue = buildIssue(creator, creator, IssueStatus.TODO);

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(permissionService.canChangeStatus(creator, issue)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        UpdateIssueRequest req = new UpdateIssueRequest(
                null, null, null, null, IssueStatus.IN_PROGRESS, null, null, null
        );
        issueService.update(issue.getId(), req, creator);

        verify(notificationService, never()).notifyUser(anyString(), anyString(), any());
    }

    /**
     * RF13 - USER non puo archiviare una issue.
     */
    @Test
    @DisplayName("USER non puo archiviare una issue")
    void whenUserTriesToArchive_thenThrowForbidden() {
        User user = buildUser("user", GlobalRole.USER);
        Issue issue = buildIssue(user, user, IssueStatus.TODO);

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(permissionService.canArchive(user)).thenReturn(false);

        UpdateIssueRequest req = new UpdateIssueRequest(
                null, null, null, null, null, null, null, true
        );
        assertThrows(AccessDeniedException.class,
                () -> issueService.update(issue.getId(), req, user));
    }

    /**
     * RF13 - ADMIN puo archiviare una issue.
     */
    @Test
    @DisplayName("ADMIN puo archiviare una issue")
    void whenAdminArchives_thenSuccess() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        Issue issue = buildIssue(buildUser("creator", GlobalRole.USER), admin, IssueStatus.RISOLTA);

        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(permissionService.canArchive(admin)).thenReturn(true);
        when(issueRepository.save(any(Issue.class))).thenReturn(issue);

        UpdateIssueRequest req = new UpdateIssueRequest(
                null, null, null, null, null, null, null, true
        );
        IssueResponse response = issueService.update(issue.getId(), req, admin);

        assertNotNull(response);
        assertTrue(issue.isArchived());
        assertNotNull(issue.getArchivedAt());
        assertEquals(admin, issue.getArchivedBy());
    }

    // --- Helper methods ---

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

    private Issue buildIssue(User creator, User assignee, IssueStatus status) {
        Issue issue = new Issue();
        issue.setId("issue-id");
        issue.setTitle("Test Issue Title");
        issue.setType(IssueType.BUG);
        issue.setDescription("Una descrizione abbastanza lunga per il test");
        issue.setStatus(status);
        issue.setCreatedBy(creator);
        issue.setAssignedTo(assignee);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        issue.setLabels(new ArrayList<>());
        issue.setHistory(new ArrayList<>());
        return issue;
    }
}
