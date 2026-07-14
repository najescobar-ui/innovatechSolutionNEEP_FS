package ${package}.dto;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/** Standardized error response for every endpoint of the service. */
public record ErrorResponse(String timestamp, int status, String error, String message, String path) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now().toString(), status.value(), status.getReasonPhrase(), message, path);
    }
}
