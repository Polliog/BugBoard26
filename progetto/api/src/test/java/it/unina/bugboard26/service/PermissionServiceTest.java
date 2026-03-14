package it.unina.bugboard26.service;

import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.enums.IssueStatus;
import it.unina.bugboard26.enums.IssueType;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF06, RF13, RF15 — Test per PermissionService.canModifyIssue(User user, Issue issue).
 * Logica di accesso basata su ruolo e ownership (createdBy).
 */
class PermissionServiceTest {

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();
    }

    /**
     * RF06/RF13 — Un ADMIN ha sempre il permesso di modificare qualsiasi issue,
     * indipendentemente da chi l'ha creata.
     */
    @Test
    @DisplayName("ADMIN può modificare issue create da qualsiasi utente")
    void adminCanModifyIssueCreatedByAnotherUser() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        User owner = buildUser("owner", GlobalRole.USER);
        Issue issue = buildIssue(owner, IssueStatus.TODO);

        assertTrue(permissionService.canModifyIssue(admin, issue));
    }

    /**
     * RF06 — Un USER può modificare le issue che ha creato lui stesso:
     * il check confronta issue.createdBy.id con user.id.
     */
    @Test
    @DisplayName("USER può modificare la propria issue (createdBy == user)")
    void userCanModifyIssueTheyCreated() {
        User user = buildUser("user", GlobalRole.USER);
        Issue issue = buildIssue(user, IssueStatus.IN_PROGRESS);

        assertTrue(permissionService.canModifyIssue(user, issue));
    }

    /**
     * RF15 — Un USER non può modificare issue create da un altro utente:
     * l'ID del creatore non coincide con quello dell'attore.
     */
    @Test
    @DisplayName("USER non può modificare issue create da un altro utente")
    void userCannotModifyIssueCreatedBySomeoneElse() {
        User user = buildUser("user", GlobalRole.USER);
        User other = buildUser("other", GlobalRole.USER);
        Issue issue = buildIssue(other, IssueStatus.TODO);

        assertFalse(permissionService.canModifyIssue(user, issue));
    }

    // --- Helpers ---

    private User buildUser(String name, GlobalRole role) {
        User user = new User();
        user.setId(name + "-id");
        user.setEmail(name + "@test.com");
        user.setName(name);
        user.setRole(role);
        user.setCreatedAt(Instant.now());
        return user;
    }

    private Issue buildIssue(User creator, IssueStatus status) {
        Issue issue = new Issue();
        issue.setId("issue-id");
        issue.setTitle("Test Issue");
        issue.setType(IssueType.BUG);
        issue.setDescription("Descrizione di test");
        issue.setStatus(status);
        issue.setCreatedBy(creator);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        issue.setLabels(new ArrayList<>());
        issue.setHistory(new ArrayList<>());
        return issue;
    }
}
