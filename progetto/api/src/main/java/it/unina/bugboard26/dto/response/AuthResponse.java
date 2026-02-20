package it.unina.bugboard26.dto.response;

/**
 * DTO di risposta per l'autenticazione.
 * RF01 - Login.
 */
public record AuthResponse(String token, UserResponse user) {}
