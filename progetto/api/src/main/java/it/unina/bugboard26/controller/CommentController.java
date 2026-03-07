package it.unina.bugboard26.controller;

import it.unina.bugboard26.dto.request.CreateCommentRequest;
import it.unina.bugboard26.dto.request.UpdateCommentRequest;
import it.unina.bugboard26.dto.response.CommentResponse;

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

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getByIssue(@PathVariable String issueId) {
        return ResponseEntity.ok(commentService.getByIssue(issueId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(@PathVariable String issueId,
                                                   @Valid @RequestBody CreateCommentRequest request,
                                                   Authentication authentication) {
        CommentResponse response = commentService.create(issueId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommentResponse> update(@PathVariable String issueId,
                                                   @PathVariable String id,
                                                   @Valid @RequestBody UpdateCommentRequest request,
                                                   Authentication authentication) {
        CommentResponse response = commentService.update(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String issueId,
                                        @PathVariable String id,
                                        Authentication authentication) {
        commentService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
