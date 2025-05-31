package com.inertia.chat.common.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public EnvelopeResponse<?> handleAuthException(AuthenticationCredentialsNotFoundException ex) {
        return EnvelopeResponse.error(Collections.singletonList("Invalid or expired token. Please log in again."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public EnvelopeResponse<?> handleAccessDeniedException(AccessDeniedException ex) {
        return EnvelopeResponse.error(Collections.singletonList("Access denied."));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<EnvelopeResponse<?>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EnvelopeResponse.error(Collections.singletonList(ex.getMessage())));
    }
}