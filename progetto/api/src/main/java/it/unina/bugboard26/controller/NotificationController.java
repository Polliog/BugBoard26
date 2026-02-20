package it.unina.bugboard26.controller;

import it.unina.bugboard26.dto.response.NotificationResponse;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.service.AuthService;
import it.unina.bugboard26.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per la gestione delle notifiche.
 * RF06 - Notifiche cambio stato.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService,
                                   AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    /**
     * RF06 - Restituisce le notifiche dell'utente corrente.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(Authentication authentication) {
        User currentUser = authService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(notificationService.getByUser(currentUser.getId()));
    }

    /**
     * RF06 - Segna una notifica come letta.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }
}
