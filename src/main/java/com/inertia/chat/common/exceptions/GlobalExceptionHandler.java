package com.inertia.chat.common.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.inertia.chat.common.dto.EnvelopeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Object> errorMessages = ex.getBindingResult().getAllErrors().stream()
            .map(error -> ((FieldError) error).getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
            
        log.error("Validation error: {}", errorMessages);
        return ResponseEntity
            .badRequest()
            .body(EnvelopeResponse.error(errorMessages));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        List<Object> errorMessage = new ArrayList<>();
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            String fieldName = !ife.getPath().isEmpty() ? ife.getPath().get(0).getFieldName() : "unknown";
            String value = ife.getValue().toString();
            String message = String.format("Invalid value '%s' for field '%s'", value, fieldName);
            log.error("JSON parse error: {}", message);
            errorMessage.add(message);
        } else {
            errorMessage.add("Invalid request format");
        }
        return ResponseEntity
            .badRequest()
            .body(EnvelopeResponse.error(errorMessage));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseEntity
            .badRequest()
            .body(EnvelopeResponse.error(List.of(ex.getMessage())));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleAuthException(AuthenticationCredentialsNotFoundException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(EnvelopeResponse.error(Collections.singletonList("Invalid or expired token. Please log in again.")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(EnvelopeResponse.error(Collections.singletonList("Access denied.")));
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleNotFound(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(EnvelopeResponse.error(Collections.singletonList(ex.getMessage())));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleRuntimeException(RuntimeException ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(EnvelopeResponse.error(List.of("An unexpected error occurred")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleGenericException(Exception ex) {
        log.error("Generic error: ", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(EnvelopeResponse.error(Collections.singletonList(ex.getMessage())));
    }
}