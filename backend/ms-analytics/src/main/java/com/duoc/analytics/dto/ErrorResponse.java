package com.duoc.analytics.dto;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/** Respuesta de error estandarizada para todos los endpoints del servicio. */
public record ErrorResponse(String timestamp, int status, String error, String message, String path) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now().toString(), status.value(), status.getReasonPhrase(), message, path);
    }
}
