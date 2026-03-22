package com.example.busticket.config;

import com.example.busticket.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<MessageResponse> json(HttpStatus status, String msg) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new MessageResponse(msg));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<MessageResponse> handleResponseStatus(ResponseStatusException ex) {
        String reason = ex.getReason();
        if (reason == null || reason.isBlank()) {
            reason = ex.getMessage();
        }
        if (reason == null) reason = "Request failed";
        return json(HttpStatus.valueOf(ex.getStatusCode().value()), reason);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError == null ? "Validation failed"
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return json(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponse> handleAccessDenied(AccessDeniedException ex) {
        return json(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneric(Exception ex) {
        return json(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + ex.getMessage());
    }
}

