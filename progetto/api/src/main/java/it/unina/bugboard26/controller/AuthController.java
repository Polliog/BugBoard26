package it.unina.bugboard26.controller;

import it.unina.bugboard26.dto.request.LoginRequest;
import it.unina.bugboard26.dto.response.AuthResponse;
import it.unina.bugboard26.dto.response.UserResponse;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller per l'autenticazione.
 * RF01 - Login e recupero utente corrente.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * RF01 - Login con email e password, restituisce token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * RF01 - Restituisce i dati dell'utente corrente (dal token).
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        User user = authService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
