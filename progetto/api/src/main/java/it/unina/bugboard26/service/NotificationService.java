package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.response.NotificationResponse;
import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.Notification;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.NotificationRepository;
import it.unina.bugboard26.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<NotificationResponse> getByUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Notifica non trovata"));

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.from(saved);
    }

    @Transactional
    public void notifyUser(String userId, String message, Issue issue) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente non trovato"));

        Notification notification = new Notification(user, issue, message);
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyUser(String userId, String message) {
        notifyUser(userId, message, null);
    }
}
