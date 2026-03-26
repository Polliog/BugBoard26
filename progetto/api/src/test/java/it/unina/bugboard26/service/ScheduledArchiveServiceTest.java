package it.unina.bugboard26.service;

import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.enums.IssueStatus;
import it.unina.bugboard26.enums.IssueType;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.IssueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledArchiveServiceTest {

    @Mock private IssueRepository issueRepository;

    private ScheduledArchiveService createService(boolean enabled, int days) {
        return new ScheduledArchiveService(issueRepository, enabled, days);
    }

    @Test
    @DisplayName("Auto-archive archivia issue risolte piu' vecchie di 30 giorni")
    void whenResolvedIssueOlderThan30Days_thenArchived() {
        Issue issue = buildIssue("issue-1", IssueStatus.RISOLTA);

        when(issueRepository.findResolvedBeforeCutoff(any(Instant.class)))
                .thenReturn(List.of(issue));

        ScheduledArchiveService service = createService(true, 30);
        service.archiveResolvedIssues();

        assertTrue(issue.isArchived());
        assertNotNull(issue.getArchivedAt());
        assertNull(issue.getArchivedBy());
        assertFalse(issue.getHistory().isEmpty());
        assertEquals("Archiviata automaticamente dopo 30 giorni", issue.getHistory().get(0).getAction());
        verify(issueRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Auto-archive non fa nulla se disabilitato")
    void whenDisabled_thenNoAction() {
        ScheduledArchiveService service = createService(false, 30);
        service.archiveResolvedIssues();

        verify(issueRepository, never()).findResolvedBeforeCutoff(any());
        verify(issueRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Auto-archive non fa nulla se non ci sono issue da archiviare")
    void whenNoResolvedIssues_thenNoSave() {
        when(issueRepository.findResolvedBeforeCutoff(any(Instant.class)))
                .thenReturn(List.of());

        ScheduledArchiveService service = createService(true, 30);
        service.archiveResolvedIssues();

        verify(issueRepository, never()).saveAll(anyList());
    }

    private Issue buildIssue(String id, IssueStatus status) {
        User creator = new User();
        creator.setId("creator-id");
        creator.setEmail("creator@test.com");
        creator.setPasswordHash("hashed");
        creator.setName("creator");
        creator.setRole(GlobalRole.USER);
        creator.setCreatedAt(Instant.now());

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
        issue.setAttachments(new ArrayList<>());
        return issue;
    }
}
