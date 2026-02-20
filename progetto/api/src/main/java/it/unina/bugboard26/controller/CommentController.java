package it.unina.bugboard26.controller;

import it.unina.bugboard26.dto.request.CreateCommentRequest;
import it.unina.bugboard26.dto.request.UpdateCommentRequest;
import it.unina.bugboard26.dto.response.CommentResponse;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.service.AuthService;
import it.unina.bugboard26.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    public CommentController(CommentService commentService, AuthService authService) {
        this.commentService = commentService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getByIssue(@PathVariable String issueId) {
        return ResponseEntity.ok(commentService.getByIssue(issueId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(@PathVariable String issueId,
                                                   @Valid @RequestBody CreateCommentRequest request,
                                                   Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        CommentResponse response = commentService.create(issueId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommentResponse> update(@PathVariable String issueId,
                                                   @PathVariable String id,
                                                   @Valid @RequestBody UpdateCommentRequest request,
                                                   Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        CommentResponse response = commentService.update(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String issueId,
                                        @PathVariable String id,
                                        Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        commentService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
