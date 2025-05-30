package com.inertia.chat.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvelopeResponse<T> {
    private T data;
    private String message;
    private List<Object> errors;

    public static <T> EnvelopeResponse<T> success(T data, String message) {
        return new EnvelopeResponse<>(data, message, null);
    }

    public static <T> EnvelopeResponse<T> error(List<Object> errors) {
        return new EnvelopeResponse<>(null, null, errors);
    }
}