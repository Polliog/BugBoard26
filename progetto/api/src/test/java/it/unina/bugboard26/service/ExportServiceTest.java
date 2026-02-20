package it.unina.bugboard26.service;

import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test per ExportService.
 * RF08 - Export CSV/PDF.
 */
@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private IssueService issueService;

    @InjectMocks
    private ExportService exportService;

    @Test
    @DisplayName("Export CSV genera contenuto valido")
    void exportCsvGeneratesValidContent() {
        User creator = buildUser("creator", GlobalRole.USER);
        List<Issue> issues = List.of(
                buildIssue("Issue 1", IssueType.BUG, IssuePriority.ALTA, creator),
                buildIssue("Issue 2", IssueType.FEATURE, IssuePriority.MEDIA, creator)
        );

        when(issueService.getFilteredForExport(any(), any(), any(), any(), any(), any()))
                .thenReturn(issues);

        byte[] result = exportService.exportCsv(null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);

        String csv = new String(result);
        assertTrue(csv.contains("Issue 1"));
        assertTrue(csv.contains("Issue 2"));
        assertTrue(csv.contains("BUG"));
        assertTrue(csv.contains("FEATURE"));
    }

    @Test
    @DisplayName("Export CSV con lista vuota genera solo header")
    void exportCsvWithEmptyListGeneratesOnlyHeader() {
        when(issueService.getFilteredForExport(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        byte[] result = exportService.exportCsv(null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);

        String csv = new String(result);
        assertTrue(csv.contains("ID"));
        assertTrue(csv.contains("Titolo"));
    }

    @Test
    @DisplayName("Export PDF genera contenuto valido")
    void exportPdfGeneratesValidContent() {
        User creator = buildUser("creator", GlobalRole.USER);
        List<Issue> issues = List.of(
                buildIssue("Issue PDF", IssueType.BUG, IssuePriority.CRITICA, creator)
        );

        when(issueService.getFilteredForExport(any(), any(), any(), any(), any(), any()))
                .thenReturn(issues);

        byte[] result = exportService.exportPdf(null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.length > 0);
        // I file PDF iniziano con %PDF
        String header = new String(result, 0, Math.min(5, result.length));
        assertTrue(header.startsWith("%PDF"));
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

    private Issue buildIssue(String title, IssueType type, IssuePriority priority, User creator) {
        Issue issue = new Issue();
        issue.setId(title.toLowerCase().replace(" ", "-"));
        issue.setTitle(title);
        issue.setType(type);
        issue.setDescription("Descrizione test sufficientemente lunga per validazione");
        issue.setPriority(priority);
        issue.setStatus(IssueStatus.APERTA);
        issue.setCreatedBy(creator);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        issue.setLabels(new ArrayList<>());
        issue.setHistory(new ArrayList<>());
        return issue;
    }
}
