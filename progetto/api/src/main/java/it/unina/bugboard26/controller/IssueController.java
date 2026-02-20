package it.unina.bugboard26.controller;

import it.unina.bugboard26.dto.request.CreateIssueRequest;
import it.unina.bugboard26.dto.request.UpdateIssueRequest;
import it.unina.bugboard26.dto.response.IssueResponse;
import it.unina.bugboard26.dto.response.PagedResponse;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.IssuePriority;
import it.unina.bugboard26.model.enums.IssueStatus;
import it.unina.bugboard26.model.enums.IssueType;
import it.unina.bugboard26.service.AuthService;
import it.unina.bugboard26.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per la gestione delle issue.
 * RF02, RF03, RF06, RF11, RF13.
 */
@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;
    private final AuthService authService;

    public IssueController(IssueService issueService, AuthService authService) {
        this.issueService = issueService;
        this.authService = authService;
    }

    /**
     * RF03, RF11 - Lista issue con filtri, ricerca e paginazione.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<IssueResponse>> getAll(
            @RequestParam(required = false) List<IssueType> type,
            @RequestParam(required = false) List<IssueStatus> status,
            @RequestParam(required = false) List<IssuePriority> priority,
            @RequestParam(required = false) String assignedToId,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        PagedResponse<IssueResponse> response = issueService.getAll(
                type, status, priority, assignedToId, archived, search, pageable
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Recupera una singola issue per ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(issueService.getById(id));
    }

    /**
     * RF02 - Crea una nuova issue.
     */
    @PostMapping
    public ResponseEntity<IssueResponse> create(@Valid @RequestBody CreateIssueRequest request,
                                                 Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        IssueResponse response = issueService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * RF06, RF13 - Aggiorna una issue (semantica PATCH).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<IssueResponse> update(@PathVariable String id,
                                                @Valid @RequestBody UpdateIssueRequest request,
                                                Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        IssueResponse response = issueService.update(id, request, currentUser);
        return ResponseEntity.ok(response);
    }
}
