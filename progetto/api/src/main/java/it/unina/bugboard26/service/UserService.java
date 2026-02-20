package it.unina.bugboard26.service;

import it.unina.bugboard26.dto.request.CreateUserRequest;
import it.unina.bugboard26.dto.request.UpdateUserRequest;
import it.unina.bugboard26.dto.response.UserResponse;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(CONFLICT, "Email gia' in uso");
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.role()
        );

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse update(String id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utente non trovato"));

        if (request.email() != null) {
            if (!request.email().equals(user.getEmail()) && userRepository.existsByEmail(request.email())) {
                throw new ResponseStatusException(CONFLICT, "Email gia' in uso");
            }
            user.setEmail(request.email());
        }
        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional
    public void delete(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Utente non trovato");
        }
        userRepository.deleteById(id);
    }
}
