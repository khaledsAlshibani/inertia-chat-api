package com.inertia.chat.common.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.inertia.chat.common.dto.EnvelopeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        List<Object> errorMessages = errors.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
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
            String fieldName = ife.getPath().get(0).getFieldName();
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<EnvelopeResponse<List<Object>>> handleRuntimeException(RuntimeException ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(EnvelopeResponse.error(List.of("An unexpected error occurred")));
    }
}