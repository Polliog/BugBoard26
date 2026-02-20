package it.unina.bugboard26.service;

import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.GlobalRole;
import it.unina.bugboard26.model.enums.IssueStatus;
import it.unina.bugboard26.model.enums.IssueType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per PermissionService.
 * Verifica tutti i metodi canX().
 */
class PermissionServiceTest {

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();
    }

    // --- canCreateIssue ---

    @Test
    @DisplayName("ADMIN puo creare issue")
    void adminCanCreateIssue() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canCreateIssue(admin));
    }

    @Test
    @DisplayName("USER puo creare issue")
    void userCanCreateIssue() {
        User user = buildUser("user", GlobalRole.USER);
        assertTrue(permissionService.canCreateIssue(user));
    }

    @Test
    @DisplayName("EXTERNAL non puo creare issue")
    void externalCannotCreateIssue() {
        User external = buildUser("external", GlobalRole.EXTERNAL);
        assertFalse(permissionService.canCreateIssue(external));
    }

    // --- canModifyIssue ---

    @Test
    @DisplayName("ADMIN puo modificare qualsiasi issue")
    void adminCanModifyAnyIssue() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        User creator = buildUser("creator", GlobalRole.USER);
        Issue issue = buildIssue(creator, null);
        assertTrue(permissionService.canModifyIssue(admin, issue));
    }

    @Test
    @DisplayName("USER puo modificare issue assegnata a se stesso")
    void userCanModifyAssignedIssue() {
        User user = buildUser("user", GlobalRole.USER);
        Issue issue = buildIssue(buildUser("other", GlobalRole.USER), user);
        assertTrue(permissionService.canModifyIssue(user, issue));
    }

    @Test
    @DisplayName("USER non puo modificare issue non assegnata a se stesso")
    void userCannotModifyUnassignedIssue() {
        User user = buildUser("user", GlobalRole.USER);
        User other = buildUser("other", GlobalRole.USER);
        Issue issue = buildIssue(other, other);
        assertFalse(permissionService.canModifyIssue(user, issue));
    }

    @Test
    @DisplayName("EXTERNAL non puo modificare issue")
    void externalCannotModifyIssue() {
        User external = buildUser("external", GlobalRole.EXTERNAL);
        Issue issue = buildIssue(buildUser("creator", GlobalRole.USER), external);
        assertFalse(permissionService.canModifyIssue(external, issue));
    }

    // --- canArchive ---

    @Test
    @DisplayName("ADMIN puo archiviare")
    void adminCanArchive() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canArchive(admin));
    }

    @Test
    @DisplayName("USER non puo archiviare")
    void userCannotArchive() {
        User user = buildUser("user", GlobalRole.USER);
        assertFalse(permissionService.canArchive(user));
    }

    @Test
    @DisplayName("EXTERNAL non puo archiviare")
    void externalCannotArchive() {
        User external = buildUser("external", GlobalRole.EXTERNAL);
        assertFalse(permissionService.canArchive(external));
    }

    // --- canComment ---

    @Test
    @DisplayName("ADMIN puo commentare")
    void adminCanComment() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canComment(admin));
    }

    @Test
    @DisplayName("USER puo commentare")
    void userCanComment() {
        User user = buildUser("user", GlobalRole.USER);
        assertTrue(permissionService.canComment(user));
    }

    @Test
    @DisplayName("EXTERNAL non puo commentare")
    void externalCannotComment() {
        User external = buildUser("external", GlobalRole.EXTERNAL);
        assertFalse(permissionService.canComment(external));
    }

    // --- canManageUsers ---

    @Test
    @DisplayName("ADMIN puo gestire utenti")
    void adminCanManageUsers() {
        User admin = buildUser("admin", GlobalRole.ADMIN);
        assertTrue(permissionService.canManageUsers(admin));
    }

    @Test
    @DisplayName("USER non puo gestire utenti")
    void userCannotManageUsers() {
        User user = buildUser("user", GlobalRole.USER);
        assertFalse(permissionService.canManageUsers(user));
    }

    // --- Helper methods ---

    private User buildUser(String name, GlobalRole role) {
        User user = new User();
        user.setId(name + "-id");
        user.setEmail(name + "@test.com");
        user.setName(name);
        user.setRole(role);
        return user;
    }

    private Issue buildIssue(User creator, User assignee) {
        Issue issue = new Issue();
        issue.setId("issue-id");
        issue.setTitle("Test Issue");
        issue.setType(IssueType.BUG);
        issue.setDescription("Descrizione test sufficientemente lunga");
        issue.setStatus(IssueStatus.TODO);
        issue.setCreatedBy(creator);
        issue.setAssignedTo(assignee);
        return issue;
    }
}
