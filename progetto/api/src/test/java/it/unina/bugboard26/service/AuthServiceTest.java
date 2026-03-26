package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.LoginRequest;
import it.unina.bugboard26.dto.response.AuthResponse;
import it.unina.bugboard26.dto.response.UserResponse;
import it.unina.bugboard26.enums.GlobalRole;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.UserRepository;
import it.unina.bugboard26.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RF01 — Test per AuthService: login e getMe.
 * Verifica autenticazione JWT, normalizzazione email e gestione errori credenziali.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    /**
     * RF01 — Login con credenziali valide deve restituire un AuthResponse
     * contenente il token JWT e i dati utente corretti.
     */
    @Test
    @DisplayName("Login con credenziali valide restituisce token e dati utente")
    void whenLoginWithValidCredentials_thenReturnsAuthResponse() {
        User user = buildUser("mario", GlobalRole.USER);
        LoginRequest request = new LoginRequest("mario@test.com", "password123");

        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token-123");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.token());
        assertEquals(user.getId(), response.user().id());
        assertEquals(user.getEmail(), response.user().email());
        assertEquals(user.getName(), response.user().name());
        assertEquals(GlobalRole.USER, response.user().role());
        verify(jwtService).generateToken(user);
    }

    /**
     * RF01 — Login con password errata deve lanciare ResponseStatusException
     * con stato 401 UNAUTHORIZED, senza generare alcun token.
     */
    @Test
    @DisplayName("Login con password errata lancia UNAUTHORIZED")
    void whenLoginWithWrongPassword_thenThrowsUnauthorized() {
        User user = buildUser("mario", GlobalRole.USER);
        LoginRequest request = new LoginRequest("mario@test.com", "wrongpassword");

        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", user.getPasswordHash())).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(request));
        assertEquals(401, ex.getStatusCode().value());
        verify(jwtService, never()).generateToken(any());
    }

    /**
     * RF01 — Login con email inesistente deve lanciare ResponseStatusException
     * con stato 401 UNAUTHORIZED prima di qualsiasi controllo password.
     */
    @Test
    @DisplayName("Login con email inesistente lancia UNAUTHORIZED")
    void whenLoginWithNonExistentEmail_thenThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("nobody@test.com", "password123");

        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(request));
        assertEquals(401, ex.getStatusCode().value());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    /**
     * RF01 — Login normalizza l'email a lowercase prima della ricerca:
     * un'email con maiuscole deve essere cercata nel repository in minuscolo.
     */
    @Test
    @DisplayName("Login normalizza email a lowercase prima della ricerca")
    void whenLoginWithUppercaseEmail_thenSearchesLowercase() {
        User user = buildUser("mario", GlobalRole.USER);
        LoginRequest request = new LoginRequest("MARIO@TEST.COM", "password123");

        when(userRepository.findByEmail("mario@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(userRepository).findByEmail("mario@test.com");
        assertNotNull(response);
    }

    /**
     * RF01 — getMe con email valida restituisce il UserResponse corretto
     * senza mai esporre la passwordHash.
     */
    @Test
    @DisplayName("getMe con email valida restituisce UserResponse")
    void whenGetMeWithValidEmail_thenReturnsUserResponse() {
        User user = buildUser("admin", GlobalRole.ADMIN);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        UserResponse response = authService.getMe("admin@test.com");

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getName(), response.name());
        assertEquals(GlobalRole.ADMIN, response.role());
    }

    /**
     * RF01 — getMe con email inesistente deve lanciare ResponseStatusException
     * con stato 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("getMe con email inesistente lancia UNAUTHORIZED")
    void whenGetMeWithNonExistentEmail_thenThrowsUnauthorized() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.getMe("ghost@test.com"));
        assertEquals(401, ex.getStatusCode().value());
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
