package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.CreateUserRequest;
import it.unina.bugboard26.dto.request.UpdateUserRequest;
import it.unina.bugboard26.dto.response.UserResponse;
import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RF01 — Test per UserService: CRUD utenti (solo ADMIN).
 * Verifica creazione, aggiornamento PATCH, cancellazione e unicita' email.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    /**
     * RF01 — Creazione utente con dati validi: l'email viene normalizzata,
     * la password codificata e il risultato mappato correttamente a UserResponse.
     */
    @Test
    @DisplayName("Creazione utente con dati validi salva e restituisce UserResponse")
    void whenCreateWithValidData_thenSavesAndReturnsUserResponse() {
        CreateUserRequest request = new CreateUserRequest(
                "luigi@test.com", "password123", "Luigi", GlobalRole.USER
        );
        User saved = buildUser("luigi", GlobalRole.USER);

        when(userRepository.existsByEmail("luigi@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertEquals(saved.getId(), response.id());
        assertEquals(saved.getEmail(), response.email());
        assertEquals(saved.getName(), response.name());
        assertEquals(GlobalRole.USER, response.role());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    /**
     * RF01 — Creazione utente con email gia' esistente deve lanciare
     * ResponseStatusException con stato 409 CONFLICT senza salvare.
     */
    @Test
    @DisplayName("Creazione utente con email duplicata lancia CONFLICT")
    void whenCreateWithDuplicateEmail_thenThrowsConflict() {
        CreateUserRequest request = new CreateUserRequest(
                "existing@test.com", "password123", "Existing", GlobalRole.USER
        );

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.create(request));
        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    /**
     * RF01 — Creazione utente normalizza l'email a lowercase:
     * un'email con maiuscole deve essere convertita prima del check unicita'.
     */
    @Test
    @DisplayName("Creazione utente normalizza email a lowercase")
    void whenCreateWithUppercaseEmail_thenNormalizesToLowercase() {
        CreateUserRequest request = new CreateUserRequest(
                "LUIGI@TEST.COM", "password123", "Luigi", GlobalRole.USER
        );
        User saved = buildUser("luigi", GlobalRole.USER);

        when(userRepository.existsByEmail("luigi@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        userService.create(request);

        verify(userRepository).existsByEmail("luigi@test.com");
    }

    /**
     * RF01 — Update PATCH: solo i campi non-null vengono aggiornati.
     * Se si passa solo il nome, email, ruolo e password restano invariati.
     */
    @Test
    @DisplayName("Update PATCH aggiorna solo i campi non-null")
    void whenUpdateWithPartialFields_thenOnlyNonNullFieldsAreChanged() {
        User existing = buildUser("mario", GlobalRole.USER);
        String originalEmail = existing.getEmail();
        GlobalRole originalRole = existing.getRole();

        UpdateUserRequest request = new UpdateUserRequest(null, "Mario Rossi", null, null);

        when(userRepository.findById("mario-id")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        UserResponse response = userService.update("mario-id", request);

        assertEquals("Mario Rossi", existing.getName());
        assertEquals(originalEmail, existing.getEmail());
        assertEquals(originalRole, existing.getRole());
        verify(passwordEncoder, never()).encode(any());
    }

    /**
     * RF01 — Update con email gia' in uso da un altro utente deve lanciare
     * ResponseStatusException con stato 409 CONFLICT.
     */
    @Test
    @DisplayName("Update con email duplicata di un altro utente lancia CONFLICT")
    void whenUpdateWithDuplicateEmail_thenThrowsConflict() {
        User existing = buildUser("mario", GlobalRole.USER);
        UpdateUserRequest request = new UpdateUserRequest("taken@test.com", null, null, null);

        when(userRepository.findById("mario-id")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.update("mario-id", request));
        assertEquals(409, ex.getStatusCode().value());
        verify(userRepository, never()).save(any());
    }

    /**
     * RF01 — Cancellazione di utente inesistente deve lanciare
     * ResponseStatusException con stato 404 NOT_FOUND.
     */
    @Test
    @DisplayName("Cancellazione utente inesistente lancia NOT_FOUND")
    void whenDeleteNonExistentUser_thenThrowsNotFound() {
        when(userRepository.existsById("ghost-id")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.delete("ghost-id"));
        assertEquals(404, ex.getStatusCode().value());
        verify(userRepository, never()).deleteById(any());
    }

    /**
     * RF01 — getAll restituisce tutti gli utenti mappati a UserResponse,
     * preservando l'ordine del repository.
     */
    @Test
    @DisplayName("getAll restituisce tutti gli utenti come UserResponse")
    void whenGetAll_thenReturnsAllUsersMappedToUserResponse() {
        User user1 = buildUser("alice", GlobalRole.ADMIN);
        User user2 = buildUser("bob", GlobalRole.USER);
        User user3 = buildUser("carol", GlobalRole.EXTERNAL);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        List<UserResponse> result = userService.getAll();

        assertEquals(3, result.size());
        assertEquals("alice-id", result.get(0).id());
        assertEquals("bob-id", result.get(1).id());
        assertEquals("carol-id", result.get(2).id());
        assertEquals(GlobalRole.ADMIN, result.get(0).role());
        assertEquals(GlobalRole.USER, result.get(1).role());
        assertEquals(GlobalRole.EXTERNAL, result.get(2).role());
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
}
