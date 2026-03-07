package it.unina.bugboard26.controller;

import it.unina.bugboard26.dto.request.CreateIssueRequest;
import it.unina.bugboard26.dto.request.UpdateIssueRequest;
import it.unina.bugboard26.dto.response.IssueResponse;
import it.unina.bugboard26.dto.response.PagedResponse;

import it.unina.bugboard26.model.enums.IssuePriority;
import it.unina.bugboard26.model.enums.IssueStatus;
import it.unina.bugboard26.model.enums.IssueType;

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
import java.util.Set;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "title", "status", "priority", "type"
    );

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<IssueResponse>> getAll(
            @RequestParam(required = false) List<IssueType> type,
            @RequestParam(required = false) List<IssueStatus> status,
            @RequestParam(required = false) List<IssuePriority> priority,
            @RequestParam(required = false) String assignedToId,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            Authentication authentication
    ) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }
        pageSize = Math.max(1, Math.min(pageSize, 100));
        Sort sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        if (archived == null) {
            archived = false;
        }

        PagedResponse<IssueResponse> response = issueService.getAll(
                type, status, priority, assignedToId, archived, search, deleted,
                authentication.getName(), pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getById(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(issueService.getById(id, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<IssueResponse> create(@Valid @RequestBody CreateIssueRequest request,
                                                 Authentication authentication) {
        IssueResponse response = issueService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IssueResponse> update(@PathVariable String id,
                                                @Valid @RequestBody UpdateIssueRequest request,
                                                Authentication authentication) {
        IssueResponse response = issueService.update(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication authentication) {
        issueService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<IssueResponse> restore(@PathVariable String id, Authentication authentication) {
        IssueResponse response = issueService.restore(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
