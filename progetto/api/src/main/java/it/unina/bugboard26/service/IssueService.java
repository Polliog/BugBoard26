package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.CreateIssueRequest;
import it.unina.bugboard26.dto.request.UpdateIssueRequest;
import it.unina.bugboard26.dto.response.IssueResponse;
import it.unina.bugboard26.dto.response.PagedResponse;
import it.unina.bugboard26.model.HistoryEntry;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.Label;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.IssuePriority;
import it.unina.bugboard26.model.enums.IssueStatus;
import it.unina.bugboard26.model.enums.IssueType;
import it.unina.bugboard26.repository.IssueRepository;
import it.unina.bugboard26.repository.LabelRepository;
import it.unina.bugboard26.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Servizio per la gestione delle issue.
 * RF02, RF03, RF06, RF11, RF13.
 */
@Service
@Transactional(readOnly = true)
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final PermissionService permissionService;
    private final NotificationService notificationService;

    public IssueService(IssueRepository issueRepository,
                        UserRepository userRepository,
                        LabelRepository labelRepository,
                        PermissionService permissionService,
                        NotificationService notificationService) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
        this.permissionService = permissionService;
        this.notificationService = notificationService;
    }

    /**
     * RF03, RF11 - Recupera le issue con filtri e paginazione.
     */
    public PagedResponse<IssueResponse> getAll(List<IssueType> types,
                                               List<IssueStatus> statuses,
                                               List<IssuePriority> priorities,
                                               String assignedToId,
                                               Boolean archived,
                                               String search,
                                               Pageable pageable) {
        Page<Issue> page = issueRepository.findFiltered(
                types, statuses, priorities, assignedToId, archived, search, pageable
        );
        Page<IssueResponse> responsePage = page.map(IssueResponse::from);
        return PagedResponse.of(responsePage);
    }

    /**
     * Recupera una issue per ID.
     */
    public IssueResponse getById(String id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Issue non trovata"));
        return IssueResponse.from(issue);
    }

    /**
     * RF02 - Crea una nuova issue.
     */
    @Transactional
    public IssueResponse create(CreateIssueRequest request, User currentUser) {
        if (!permissionService.canCreateIssue(currentUser)) {
            throw new AccessDeniedException("Non hai i permessi per creare issue");
        }

        Issue issue = new Issue();
        issue.setTitle(request.title());
        issue.setType(request.type());
        issue.setDescription(request.description());
        issue.setPriority(request.priority());
        issue.setStatus(IssueStatus.APERTA);
        issue.setCreatedBy(currentUser);
        issue.setImage(request.image());

        if (request.assignedToId() != null) {
            User assignee = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente assegnato non trovato"));
            issue.setAssignedTo(assignee);
        }

        // Aggiungi voce di storico per la creazione
        HistoryEntry creation = new HistoryEntry(issue, currentUser, "Issue creata");
        issue.getHistory().add(creation);

        Issue saved = issueRepository.save(issue);
        return IssueResponse.from(saved);
    }

    /**
     * RF06, RF13 - Aggiorna una issue (semantica PATCH).
     * Gestisce cambio stato e archiviazione con controlli permessi e notifiche.
     */
    @Transactional
    public IssueResponse update(String id, UpdateIssueRequest request, User currentUser) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Issue non trovata"));

        // RF13 - Archiviazione: solo ADMIN
        if (request.archived() != null) {
            if (!permissionService.canArchive(currentUser)) {
                throw new AccessDeniedException("Solo gli admin possono archiviare le issue");
            }
            issue.setArchived(request.archived());
            if (request.archived()) {
                issue.setArchivedAt(Instant.now());
                issue.setArchivedBy(currentUser);
                issue.getHistory().add(new HistoryEntry(issue, currentUser, "Issue archiviata"));
            } else {
                issue.setArchivedAt(null);
                issue.setArchivedBy(null);
                issue.getHistory().add(new HistoryEntry(issue, currentUser, "Issue ripristinata"));
            }
        }

        // RF06 - Cambio stato
        if (request.status() != null) {
            if (!permissionService.canChangeStatus(currentUser, issue)) {
                throw new AccessDeniedException("Non hai i permessi per cambiare lo stato di questa issue");
            }
            IssueStatus oldStatus = issue.getStatus();
            issue.setStatus(request.status());
            String action = "Stato cambiato: " + oldStatus + " -> " + request.status();
            issue.getHistory().add(new HistoryEntry(issue, currentUser, action));

            // Notifica il creatore del cambio stato
            if (!issue.getCreatedBy().getId().equals(currentUser.getId())) {
                notificationService.notifyUser(
                        issue.getCreatedBy().getId(),
                        "La issue \"" + issue.getTitle() + "\" e' stata " + request.status().name().toLowerCase(),
                        issue
                );
            }
        }

        // Modifica campi generici (solo se l'utente puo modificare la issue)
        if (request.title() != null || request.type() != null ||
                request.description() != null || request.priority() != null ||
                request.assignedToId() != null) {

            if (!permissionService.canModifyIssue(currentUser, issue)) {
                throw new AccessDeniedException("Non hai i permessi per modificare questa issue");
            }

            if (request.title() != null) {
                issue.setTitle(request.title());
            }
            if (request.type() != null) {
                issue.setType(request.type());
            }
            if (request.description() != null) {
                issue.setDescription(request.description());
            }
            if (request.priority() != null) {
                issue.setPriority(request.priority());
            }
            if (request.assignedToId() != null) {
                User assignee = userRepository.findById(request.assignedToId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente assegnato non trovato"));
                issue.setAssignedTo(assignee);
                issue.getHistory().add(new HistoryEntry(issue, currentUser,
                        "Assegnata a " + assignee.getName()));
            }
        }

        // Aggiorna labels
        if (request.labelIds() != null) {
            if (!permissionService.canModifyIssue(currentUser, issue)) {
                throw new AccessDeniedException("Non hai i permessi per modificare questa issue");
            }
            List<Label> labels = request.labelIds().stream()
                    .map(labelId -> labelRepository.findById(labelId)
                            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Label non trovata: " + labelId)))
                    .toList();
            issue.setLabels(labels);
        }

        Issue saved = issueRepository.save(issue);
        return IssueResponse.from(saved);
    }

    /**
     * RF08 - Recupera tutte le issue filtrate per export (senza paginazione).
     */
    public List<Issue> getFilteredForExport(List<IssueType> types,
                                            List<IssueStatus> statuses,
                                            List<IssuePriority> priorities,
                                            String assignedToId,
                                            Boolean archived,
                                            String search) {
        return issueRepository.findFilteredAll(types, statuses, priorities, assignedToId, archived, search);
    }
}
