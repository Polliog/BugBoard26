package it.unina.bugboard26.service;

import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.enums.IssuePriority;
import it.unina.bugboard26.enums.IssueStatus;
import it.unina.bugboard26.enums.IssueType;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * RF08 — Test per ExportService.exportCsv(...).
 * Verifica intestazione, contenuto righe e gestione di campi opzionali null.
 */
@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private IssueService issueService;
    @InjectMocks private ExportService exportService;

    /**
     * RF08 — L'output CSV deve contenere per ogni issue almeno
     * l'ID, il titolo, il tipo e lo stato, nelle colonne previste dall'intestazione.
     */
    @Test
    @DisplayName("Export CSV di due issue contiene i dati di entrambe le righe")
    void exportCsvWithMultipleIssues_containsAllIssueTitles() {
        User creator = buildUser("creator");
        List<Issue> issues = List.of(
                buildIssue("id-1", "Bug nel login", IssueType.BUG, IssuePriority.ALTA,
                        IssueStatus.TODO, creator, Instant.now(), false),
                buildIssue("id-2", "Feature export", IssueType.FEATURE, IssuePriority.MEDIA,
                        IssueStatus.IN_PROGRESS, creator, Instant.now(), false)
        );
        when(issueService.getFilteredForExport(any(), any(), any(), any(), any(), any()))
                .thenReturn(issues);

        byte[] csvBytes = exportService.exportCsv(null, null, null, null, null, null);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("Bug nel login"), "Il titolo della prima issue deve essere presente");
        assertTrue(csv.contains("Feature export"), "Il titolo della seconda issue deve essere presente");
        assertTrue(csv.contains("BUG"), "Il tipo BUG deve essere presente");
        assertTrue(csv.contains("FEATURE"), "Il tipo FEATURE deve essere presente");
    }

    /**
     * RF08 — La priorità è un campo opzionale: quando è null il CSV deve contenere
     * una stringa vuota nella colonna Priorita (non null letterale né eccezione).
     */
    @Test
    @DisplayName("Export CSV con priorità null produce campo vuoto nella colonna Priorita")
    void exportCsvWithNullPriority_rendersEmptyStringInPriorityColumn() {
        User creator = buildUser("creator");
        Issue issue = buildIssue("id-3", "Issue senza priorità", IssueType.QUESTION,
                null, IssueStatus.TODO, creator, Instant.now(), false);
        when(issueService.getFilteredForExport(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(issue));

        byte[] csvBytes = exportService.exportCsv(
                List.of(IssueType.QUESTION), null, null, null, null, null
        );
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("Issue senza priorità"));
        // CSVWriter scrive "" per stringa vuota: il campo priorità deve essere tra due virgole/quote vuote
        assertFalse(csv.contains("null"), "Il valore null non deve apparire nel CSV");
    }

    /**
     * RF08 — Con lista issue vuota il CSV deve contenere esattamente una riga
     * (l'intestazione) e nessuna riga dati.
     */
    @Test
    @DisplayName("Export CSV senza issue produce solo la riga di intestazione")
    void exportCsvWithEmptyList_producesOnlyHeaderRow() {
        when(issueService.getFilteredForExport(any(), any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        byte[] csvBytes = exportService.exportCsv(null, null, null, null, null, null);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("ID") && csv.contains("Titolo") && csv.contains("Stato"),
                "L'intestazione deve essere presente");
        String[] lines = csv.strip().split("\n");
        assertEquals(1, lines.length,
                "Il CSV deve contenere solo la riga header quando non ci sono issue");
    }

    // --- Helpers ---

    private User buildUser(String name) {
        User user = new User();
        user.setId(name + "-id");
        user.setEmail(name + "@test.com");
        user.setPasswordHash("hashed");
        user.setName(name);
        user.setRole(GlobalRole.USER);
        user.setCreatedAt(Instant.now());
        return user;
    }

    private Issue buildIssue(String id, String title, IssueType type, IssuePriority priority,
                              IssueStatus status, User creator, Instant createdAt, boolean archived) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle(title);
        issue.setType(type);
        issue.setDescription("Descrizione di test per validazione");
        issue.setPriority(priority);
        issue.setStatus(status);
        issue.setCreatedBy(creator);
        issue.setCreatedAt(createdAt);
        issue.setUpdatedAt(createdAt);
        issue.setArchived(archived);
        issue.setLabels(new ArrayList<>());
        issue.setHistory(new ArrayList<>());
        return issue;
    }
}
