package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.CreateCommentRequest;
import it.unina.bugboard26.dto.request.UpdateCommentRequest;
import it.unina.bugboard26.dto.response.CommentResponse;
import it.unina.bugboard26.model.Comment;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.GlobalRole;
import it.unina.bugboard26.repository.CommentRepository;
import it.unina.bugboard26.repository.IssueRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final PermissionService permissionService;

    public CommentService(CommentRepository commentRepository,
                          IssueRepository issueRepository,
                          PermissionService permissionService) {
        this.commentRepository = commentRepository;
        this.issueRepository = issueRepository;
        this.permissionService = permissionService;
    }

    public List<CommentResponse> getByIssue(String issueId) {
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse create(String issueId, CreateCommentRequest request, User currentUser) {
        if (!permissionService.canComment(currentUser)) {
            throw new AccessDeniedException("Non hai i permessi per commentare");
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Issue non trovata"));

        Comment comment = new Comment(issue, currentUser, request.content());
        comment.setImage(request.image());
        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }

    @Transactional
    public CommentResponse update(String id, UpdateCommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Commento non trovato"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Puoi modificare solo i tuoi commenti");
        }

        comment.setContent(request.content());
        if (request.image() != null) {
            comment.setImage(request.image().isEmpty() ? null : request.image());
        }
        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }

    @Transactional
    public void delete(String id, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Commento non trovato"));

        boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == GlobalRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("Non hai i permessi per eliminare questo commento");
        }

        commentRepository.delete(comment);
    }
}
