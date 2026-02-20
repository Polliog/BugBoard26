package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.LoginRequest;
import it.unina.bugboard26.dto.response.AuthResponse;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.GlobalRole;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test per AuthService.
 * RF01 - Autenticazione.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Login con credenziali valide restituisce token e utente")
    void loginWithValidCredentials() {
        User user = buildUser("admin", GlobalRole.ADMIN);
        LoginRequest request = new LoginRequest("admin@test.com", "password123");

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals(user.getId(), response.user().id());
        assertEquals(user.getEmail(), response.user().email());
        verify(jwtService).generateToken(user);
    }

    @Test
    @DisplayName("Login con email non esistente lancia eccezione")
    void loginWithNonExistentEmail() {
        LoginRequest request = new LoginRequest("unknown@test.com", "password");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Login con password errata lancia eccezione")
    void loginWithWrongPassword() {
        User user = buildUser("admin", GlobalRole.ADMIN);
        LoginRequest request = new LoginRequest("admin@test.com", "wrong-password");

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("getUserByEmail restituisce utente esistente")
    void getUserByEmailReturnsUser() {
        User user = buildUser("test", GlobalRole.USER);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = authService.getUserByEmail("test@test.com");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail lancia eccezione per email non trovata")
    void getUserByEmailThrowsForUnknownEmail() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> authService.getUserByEmail("unknown@test.com"));
    }

    private User buildUser(String name, GlobalRole role) {
        User user = new User();
        user.setId(name + "-id");
        user.setEmail(name + "@test.com");
        user.setPasswordHash("hashed-password");
        user.setName(name);
        user.setRole(role);
        user.setCreatedAt(Instant.now());
        return user;
    }
}
