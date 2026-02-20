package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.LoginRequest;
import it.unina.bugboard26.dto.response.AuthResponse;
import it.unina.bugboard26.dto.response.UserResponse;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.UserRepository;
import it.unina.bugboard26.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Credenziali non valide"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Credenziali non valide");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserResponse.from(user));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Utente non trovato"));
    }
}
