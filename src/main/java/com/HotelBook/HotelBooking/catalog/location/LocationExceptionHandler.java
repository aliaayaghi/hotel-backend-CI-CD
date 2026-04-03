package com.HotelBook.HotelBooking.catalog.location;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Component("locationGlobalExceptionHandler")
public class LocationExceptionHandler {

    // ── 404 Not Found ─────────────────────────────────────────────────────────

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFound(LocationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(404, ex.getMessage())
        );
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────

    @ExceptionHandler(LocationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleLocationAlreadyExists(LocationAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(409, ex.getMessage())
        );
    }

    // ── Shared error response shape ───────────────────────────────────────────
    // Uses the same structure as your User and Hotel handlers
    // so all error responses across the API look consistent.

    @Data
    @Builder
    public static class ErrorResponse {

        private int status;
        private String error;
        private Instant timestamp;

        public static ErrorResponse of(int status, String message) {
            return ErrorResponse.builder()
                    .status(status)
                    .error(message)
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMissingBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.of(400, "Request body is missing or malformed")
        );
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {

        // Extract the first violation message, e.g. "Latitude must be <= 90"
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath().toString()
                        .replaceAll(".*\\.", "") // strip method name prefix
                        + ": " + v.getMessage())
                .findFirst()
                .orElse("Invalid request parameter");

        return error(HttpStatus.BAD_REQUEST, message);
    }

        private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
            return ResponseEntity.status(status).body(
                    ErrorResponse.of(status.value(), message)
            );
        }

}
