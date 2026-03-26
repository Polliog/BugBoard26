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
 * Test per tutti i metodi di PermissionService.
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

    // --- canCreateIssue ---

    /** RF02 — ADMIN può creare issue */
    @Test
    @DisplayName("ADMIN può creare issue")
    void adminCanCreateIssue() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canCreateIssue(admin));
    }

    /** RF02 — USER può creare issue */
    @Test
    @DisplayName("USER può creare issue")
    void userCanCreateIssue() {
        User user = buildUser("user", GlobalRole.USER);
        assertTrue(permissionService.canCreateIssue(user));
    }

    /** RF15 — EXTERNAL non può creare issue */
    @Test
    @DisplayName("EXTERNAL non può creare issue")
    void externalCannotCreateIssue() {
        User ext = buildUser("external", GlobalRole.EXTERNAL);
        assertFalse(permissionService.canCreateIssue(ext));
    }

    // --- canChangeStatus ---

    /** RF06 — canChangeStatus delega a canModifyIssue: ADMIN può sempre cambiare stato */
    @Test
    @DisplayName("ADMIN può cambiare stato di qualsiasi issue (canChangeStatus)")
    void adminCanChangeStatus() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        User other = buildUser("other", GlobalRole.USER);
        Issue issue = buildIssue(other, IssueStatus.IN_PROGRESS);

        assertTrue(permissionService.canChangeStatus(admin, issue));
    }

    // --- canArchive ---

    /** RF13 — ADMIN può archiviare */
    @Test
    @DisplayName("ADMIN può archiviare issue")
    void adminCanArchive() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canArchive(admin));
    }

    /** RF13 — USER non può archiviare */
    @Test
    @DisplayName("USER non può archiviare issue")
    void userCannotArchive() {
        User user = buildUser("user", GlobalRole.USER);
        assertFalse(permissionService.canArchive(user));
    }

    /** RF13 — EXTERNAL non può archiviare */
    @Test
    @DisplayName("EXTERNAL non può archiviare issue")
    void externalCannotArchive() {
        User ext = buildUser("external", GlobalRole.EXTERNAL);
        assertFalse(permissionService.canArchive(ext));
    }

    // --- canComment ---

    /** RF15 — ADMIN può commentare */
    @Test
    @DisplayName("ADMIN può commentare")
    void adminCanComment() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canComment(admin));
    }

    /** RF15 — USER può commentare */
    @Test
    @DisplayName("USER può commentare")
    void userCanComment() {
        User user = buildUser("user", GlobalRole.USER);
        assertTrue(permissionService.canComment(user));
    }

    /** RF15 — EXTERNAL non può commentare */
    @Test
    @DisplayName("EXTERNAL non può commentare")
    void externalCannotComment() {
        User ext = buildUser("external", GlobalRole.EXTERNAL);
        assertFalse(permissionService.canComment(ext));
    }

    // --- canManageUsers ---

    /** RF01 — ADMIN può gestire utenti */
    @Test
    @DisplayName("ADMIN può gestire utenti")
    void adminCanManageUsers() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canManageUsers(admin));
    }

    /** RF01 — USER non può gestire utenti */
    @Test
    @DisplayName("USER non può gestire utenti")
    void userCannotManageUsers() {
        User user = buildUser("user", GlobalRole.USER);
        assertFalse(permissionService.canManageUsers(user));
    }

    // --- canDeleteIssue ---

    /** ADMIN può eliminare issue */
    @Test
    @DisplayName("ADMIN può eliminare issue")
    void adminCanDeleteIssue() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canDeleteIssue(admin));
    }

    /** USER non può eliminare issue */
    @Test
    @DisplayName("USER non può eliminare issue")
    void userCannotDeleteIssue() {
        User user = buildUser("user", GlobalRole.USER);
        assertFalse(permissionService.canDeleteIssue(user));
    }

    // --- canModifyIssue (EXTERNAL) ---

    /** RF15 — EXTERNAL non può modificare issue */
    @Test
    @DisplayName("EXTERNAL non può modificare nessuna issue")
    void externalCannotModifyIssue() {
        User ext = buildUser("external", GlobalRole.EXTERNAL);
        User creator = buildUser("creator", GlobalRole.USER);
        Issue issue = buildIssue(creator, IssueStatus.TODO);

        assertFalse(permissionService.canModifyIssue(ext, issue));
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
