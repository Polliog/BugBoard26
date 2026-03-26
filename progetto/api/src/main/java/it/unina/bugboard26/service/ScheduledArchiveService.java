package it.unina.bugboard26.service;

import it.unina.bugboard26.model.HistoryEntry;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ScheduledArchiveService {

    private final IssueRepository issueRepository;
    private final boolean enabled;
    private final int days;

    public ScheduledArchiveService(IssueRepository issueRepository,
                                   @Value("${bugboard.auto-archive.enabled:true}") boolean enabled,
                                   @Value("${bugboard.auto-archive.days:30}") int days) {
        this.issueRepository = issueRepository;
        this.enabled = enabled;
        this.days = days;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void archiveResolvedIssues() {
        if (!enabled) return;

        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        List<Issue> issues = issueRepository.findResolvedBeforeCutoff(cutoff);

        if (issues.isEmpty()) return;

        for (Issue issue : issues) {
            issue.setArchived(true);
            issue.setArchivedAt(Instant.now());
            issue.setArchivedBy(null);
            issue.getHistory().add(new HistoryEntry(issue, null,
                    "Archiviata automaticamente dopo " + days + " giorni"));
        }

        issueRepository.saveAll(issues);
    }
}
