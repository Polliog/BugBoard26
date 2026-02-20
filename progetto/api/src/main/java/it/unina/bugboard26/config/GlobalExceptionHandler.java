package it.unina.bugboard26.config;

import it.unina.bugboard26.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(status).body(new ErrorResponse(status, message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        int status = HttpStatus.FORBIDDEN.value();
        return ResponseEntity.status(status).body(new ErrorResponse(status, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(new ErrorResponse(status, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return ResponseEntity.status(status).body(new ErrorResponse(status, "Errore interno del server"));
    }
}
