package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.CreateCommentRequest;
import it.unina.bugboard26.dto.request.UpdateCommentRequest;
import it.unina.bugboard26.dto.response.CommentResponse;
import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.enums.IssueStatus;
import it.unina.bugboard26.enums.IssueType;
import it.unina.bugboard26.model.Comment;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.CommentRepository;
import it.unina.bugboard26.repository.IssueRepository;
import it.unina.bugboard26.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RF15, RF06 — Test per CommentService: creazione, modifica, eliminazione commenti
 * con verifica permessi basata su ruolo e ownership.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private IssueRepository issueRepository;
    @Mock private UserRepository userRepository;
    @Mock private PermissionService permissionService;

    @InjectMocks private CommentService commentService;

    /**
     * RF15 — Un utente EXTERNAL non può creare commenti:
     * il service deve sollevare AccessDeniedException prima di cercare la issue.
     */
    @Test
    @DisplayName("EXTERNAL non può creare commenti — AccessDeniedException")
    void whenExternalCreatesComment_thenAccessDenied() {
        User external = buildUser("ext", GlobalRole.EXTERNAL);

        when(userRepository.findByEmail(external.getEmail())).thenReturn(Optional.of(external));
        when(permissionService.canComment(external)).thenReturn(false);

        CreateCommentRequest req = new CreateCommentRequest("Un commento di test", null);

        assertThrows(AccessDeniedException.class,
                () -> commentService.create("issue-1", req, external.getEmail()));
        verify(issueRepository, never()).findById(any());
        verify(commentRepository, never()).save(any());
    }

    /**
     * RF06 — Un USER autenticato crea un commento su una issue esistente:
     * il commento viene salvato e la risposta contiene i dati corretti.
     */
    @Test
    @DisplayName("USER crea commento su issue esistente — salvataggio riuscito")
    void whenUserCreatesCommentOnExistingIssue_thenCommentIsSaved() {
        User user = buildUser("user", GlobalRole.USER);
        Issue issue = buildIssue("issue-1", user);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(permissionService.canComment(user)).thenReturn(true);
        when(issueRepository.findById("issue-1")).thenReturn(Optional.of(issue));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId("comment-1");
            c.setCreatedAt(Instant.now());
            return c;
        });

        CreateCommentRequest req = new CreateCommentRequest("Contenuto del commento", null);
        CommentResponse response = commentService.create("issue-1", req, user.getEmail());

        assertNotNull(response);
        assertEquals("comment-1", response.id());
        assertEquals("Contenuto del commento", response.content());
        assertEquals("issue-1", response.issueId());
        verify(commentRepository).save(any(Comment.class));
    }

    /**
     * RF06 — Creazione commento su issue inesistente: il service deve lanciare
     * ResponseStatusException 404 dopo aver verificato il permesso di commento.
     */
    @Test
    @DisplayName("Creazione commento su issue inesistente lancia 404 NOT_FOUND")
    void whenIssueDoesNotExist_thenCreateThrows404() {
        User user = buildUser("user", GlobalRole.USER);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(permissionService.canComment(user)).thenReturn(true);
        when(issueRepository.findById("non-existent")).thenReturn(Optional.empty());

        CreateCommentRequest req = new CreateCommentRequest("Commento orfano", null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> commentService.create("non-existent", req, user.getEmail()));
        assertEquals(404, ex.getStatusCode().value());
        verify(commentRepository, never()).save(any());
    }

    /**
     * RF06 — L'autore di un commento può modificarlo: il contenuto aggiornato
     * viene persistito e restituito nella risposta.
     */
    @Test
    @DisplayName("Autore modifica il proprio commento — aggiornamento riuscito")
    void whenAuthorUpdatesOwnComment_thenContentIsUpdated() {
        User author = buildUser("author", GlobalRole.USER);
        Comment comment = buildComment("comment-1", author, buildIssue("issue-1", author));

        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCommentRequest req = new UpdateCommentRequest("Contenuto aggiornato", null);
        CommentResponse response = commentService.update("comment-1", req, author.getEmail());

        assertEquals("Contenuto aggiornato", response.content());
        verify(commentRepository).save(comment);
    }

    /**
     * RF15 — Un utente diverso dall'autore non può modificare un commento altrui:
     * il service deve sollevare AccessDeniedException e non salvare nulla.
     */
    @Test
    @DisplayName("Non-autore non può modificare commento altrui — AccessDeniedException")
    void whenNonAuthorUpdatesComment_thenAccessDenied() {
        User author = buildUser("author", GlobalRole.USER);
        User other = buildUser("other", GlobalRole.USER);
        Comment comment = buildComment("comment-1", author, buildIssue("issue-1", author));

        when(userRepository.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));

        UpdateCommentRequest req = new UpdateCommentRequest("Tentativo di modifica", null);

        assertThrows(AccessDeniedException.class,
                () -> commentService.update("comment-1", req, other.getEmail()));
        verify(commentRepository, never()).save(any());
    }

    /**
     * RF06 — L'autore di un commento può eliminarlo:
     * il repository.delete viene invocato con il commento corretto.
     */
    @Test
    @DisplayName("Autore elimina il proprio commento — cancellazione riuscita")
    void whenAuthorDeletesOwnComment_thenCommentIsRemoved() {
        User author = buildUser("author", GlobalRole.USER);
        Comment comment = buildComment("comment-1", author, buildIssue("issue-1", author));

        when(userRepository.findByEmail(author.getEmail())).thenReturn(Optional.of(author));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));

        commentService.delete("comment-1", author.getEmail());

        verify(commentRepository).delete(comment);
    }

    /**
     * RF06 — Un ADMIN (non autore) può eliminare qualsiasi commento:
     * il ruolo ADMIN sovrascrive il vincolo di ownership.
     */
    @Test
    @DisplayName("ADMIN non-autore elimina commento altrui — cancellazione riuscita")
    void whenAdminDeletesOtherUsersComment_thenCommentIsRemoved() {
        User author = buildUser("author", GlobalRole.USER);
        User admin = buildUser("admin", GlobalRole.ADMIN);
        Comment comment = buildComment("comment-1", author, buildIssue("issue-1", author));

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));

        commentService.delete("comment-1", admin.getEmail());

        verify(commentRepository).delete(comment);
    }

    /**
     * RF15 — Un USER non-autore e non-admin non può eliminare commenti altrui:
     * il service lancia AccessDeniedException e non invoca delete.
     */
    @Test
    @DisplayName("USER non-autore non può eliminare commento altrui — AccessDeniedException")
    void whenNonAuthorNonAdminDeletesComment_thenAccessDenied() {
        User author = buildUser("author", GlobalRole.USER);
        User other = buildUser("other", GlobalRole.USER);
        Comment comment = buildComment("comment-1", author, buildIssue("issue-1", author));

        when(userRepository.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class,
                () -> commentService.delete("comment-1", other.getEmail()));
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    // --- Helpers ---

    private User buildUser(String name, GlobalRole role) {
        User user = new User();
        user.setId(name + "-id");
        user.setEmail(name + "@test.com");
        user.setPasswordHash("hashed");
        user.setName(name);
        user.setRole(role);
        user.setCreatedAt(Instant.now());
        return user;
    }

    private Issue buildIssue(String id, User creator) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setTitle("Test Issue");
        issue.setType(IssueType.BUG);
        issue.setDescription("Descrizione di test sufficientemente lunga");
        issue.setStatus(IssueStatus.TODO);
        issue.setCreatedBy(creator);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        issue.setLabels(new ArrayList<>());
        issue.setHistory(new ArrayList<>());
        return issue;
    }

    private Comment buildComment(String id, User author, Issue issue) {
        Comment comment = new Comment(issue, author, "Contenuto originale");
        comment.setId(id);
        comment.setCreatedAt(Instant.now());
        return comment;
    }
}
