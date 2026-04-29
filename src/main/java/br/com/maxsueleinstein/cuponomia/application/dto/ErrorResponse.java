package br.com.maxsueleinstein.cuponomia.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response DTO for REST API.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details,
        String path
) {
    public static ErrorResponse of(int status, String error, String message,
                                   List<String> details, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, details, path);
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return of(status, error, message, List.of(), path);
    }
}
