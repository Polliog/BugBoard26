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
import it.unina.bugboard26.service.PermissionService;
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
    private final AuthService authService;
    private final PermissionService permissionService;

    public IssueController(IssueService issueService, AuthService authService, PermissionService permissionService) {
        this.issueService = issueService;
        this.authService = authService;
        this.permissionService = permissionService;
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
        if (Boolean.TRUE.equals(deleted)) {
            User currentUser = authService.getUserByEmail(authentication.getName());
            if (!permissionService.canDeleteIssue(currentUser)) {
                deleted = null;
            }
        }
        pageSize = Math.max(1, Math.min(pageSize, 100));
        Sort sort = order.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        PagedResponse<IssueResponse> response = issueService.getAll(
                type, status, priority, assignedToId, archived, search, deleted, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getById(@PathVariable String id, Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(issueService.getById(id, currentUser));
    }

    @PostMapping
    public ResponseEntity<IssueResponse> create(@Valid @RequestBody CreateIssueRequest request,
                                                 Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        IssueResponse response = issueService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IssueResponse> update(@PathVariable String id,
                                                @Valid @RequestBody UpdateIssueRequest request,
                                                Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        IssueResponse response = issueService.update(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        issueService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<IssueResponse> restore(@PathVariable String id, Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        IssueResponse response = issueService.restore(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
