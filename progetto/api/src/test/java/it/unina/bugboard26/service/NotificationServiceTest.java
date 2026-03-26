package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.response.NotificationResponse;
import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.enums.IssueStatus;
import it.unina.bugboard26.enums.IssueType;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.Notification;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.NotificationRepository;
import it.unina.bugboard26.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RF06 — Test per NotificationService: creazione notifiche, marcatura lettura,
 * eliminazione e risoluzione utente.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private NotificationService notificationService;

    /**
     * RF06 — markAsRead imposta il flag read=true sulla notifica esistente
     * e persiste la modifica tramite il repository.
     */
    @Test
    @DisplayName("markAsRead imposta read=true e salva la notifica")
    void whenMarkAsRead_thenNotificationIsSetToRead() {
        User user = buildUser("user", GlobalRole.USER);
        Notification notification = buildNotification("notif-1", user, null);

        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead("notif-1");

        assertTrue(response.read());
        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    /**
     * RF06 — markAsRead su notifica inesistente deve lanciare
     * ResponseStatusException 404 senza tentare alcun salvataggio.
     */
    @Test
    @DisplayName("markAsRead su notifica inesistente lancia 404 NOT_FOUND")
    void whenMarkAsReadOnNonExistentNotification_thenThrows404() {
        when(notificationRepository.findById("non-existent")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.markAsRead("non-existent"));
        assertEquals(404, ex.getStatusCode().value());
        verify(notificationRepository, never()).save(any());
    }

    /**
     * RF06 — notifyUser crea una notifica con riferimento alla issue:
     * il repository salva un'entità Notification con user, issue e messaggio corretti.
     */
    @Test
    @DisplayName("notifyUser crea notifica con riferimento alla issue")
    void whenNotifyUserWithIssue_thenNotificationIsSavedWithIssueRef() {
        User user = buildUser("user", GlobalRole.USER);
        Issue issue = buildIssue("issue-1", user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        notificationService.notifyUser(user.getId(), "La issue è stata risolta", issue);

        verify(notificationRepository).save(argThat(n ->
                n.getUser().getId().equals(user.getId())
                && n.getIssue().getId().equals(issue.getId())
                && n.getMessage().equals("La issue è stata risolta")
        ));
    }

    /**
     * RF06 — notifyUser con userId inesistente deve lanciare
     * ResponseStatusException 404 e non salvare alcuna notifica.
     */
    @Test
    @DisplayName("notifyUser con userId inesistente lancia 404 NOT_FOUND")
    void whenNotifyUserWithNonExistentUserId_thenThrows404() {
        Issue issue = buildIssue("issue-1", buildUser("creator", GlobalRole.USER));

        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.notifyUser("non-existent", "Messaggio", issue));
        assertEquals(404, ex.getStatusCode().value());
        verify(notificationRepository, never()).save(any());
    }

    /**
     * RF06 — deleteNotification su id inesistente lancia 404:
     * il service verifica l'esistenza prima di procedere alla cancellazione.
     */
    @Test
    @DisplayName("deleteNotification su id inesistente lancia 404 NOT_FOUND")
    void whenDeleteNonExistentNotification_thenThrows404() {
        when(notificationRepository.existsById("non-existent")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.deleteNotification("non-existent"));
        assertEquals(404, ex.getStatusCode().value());
        verify(notificationRepository, never()).deleteById(any());
    }

    /**
     * RF06 — getByUserEmail con email inesistente lancia 404:
     * la risoluzione dell'utente fallisce prima di interrogare le notifiche.
     */
    @Test
    @DisplayName("getByUserEmail con email inesistente lancia 404 NOT_FOUND")
    void whenGetByUserEmailWithNonExistentEmail_thenThrows404() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> notificationService.getByUserEmail("nobody@test.com"));
        assertEquals(404, ex.getStatusCode().value());
        verify(notificationRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    /**
     * RF06 — markAllAsRead risolve l'utente per email e invoca il repository
     * per aggiornare in bulk tutte le notifiche non lette.
     */
    @Test
    @DisplayName("markAllAsRead risolve utente e invoca repository bulk update")
    void whenMarkAllAsRead_thenResolvesUserAndCallsRepository() {
        User user = buildUser("user", GlobalRole.USER);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        notificationService.markAllAsRead(user.getEmail());

        verify(userRepository).findByEmail(user.getEmail());
        verify(notificationRepository).markAllAsReadByUserId(user.getId());
    }

    /**
     * RF06 — deleteAllByUser risolve l'utente e cancella tutte le sue notifiche:
     * verifica la corretta propagazione dell'userId al repository.
     */
    @Test
    @DisplayName("deleteAllByUser risolve utente e cancella tutte le notifiche")
    void whenDeleteAllByUser_thenResolvesUserAndDeletesAll() {
        User user = buildUser("user", GlobalRole.USER);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        notificationService.deleteAllByUser(user.getEmail());

        verify(userRepository).findByEmail(user.getEmail());
        verify(notificationRepository).deleteAllByUserId(user.getId());
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

    private Notification buildNotification(String id, User user, Issue issue) {
        Notification notification = new Notification(user, issue, "Messaggio di test");
        notification.setId(id);
        notification.setCreatedAt(Instant.now());
        return notification;
    }
}
