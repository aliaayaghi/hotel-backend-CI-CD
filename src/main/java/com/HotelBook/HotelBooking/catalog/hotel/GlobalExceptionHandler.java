package com.HotelBook.HotelBooking.catalog.hotel;

import com.HotelBook.HotelBooking.catalog.location.LocationAlreadyExistsException;
import com.HotelBook.HotelBooking.catalog.location.LocationNotFoundException;
import com.HotelBook.HotelBooking.catalog.user.exception.DuplicateEmailException;
import com.HotelBook.HotelBooking.catalog.user.exception.InvalidCredentialsException;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * Catches all RuntimeExceptions thrown from any controller and
 * converts them to a consistent JSON error response shape:
 *
 * {
 *   "timestamp": "2025-01-01T10:00:00Z",
 *   "status":    404,
 *   "error":     "Not Found",
 *   "message":   "Hotel not found with id: ..."
 * }
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * TEAM NOTE: M3 will likely own this file in the final project.
 * Share the exception class names below with M3 so they can register handlers
 * for each one. Class names to share:
 *   com.hotelbook.catalog.hotel.exception.HotelNotFoundException
 *   com.hotelbook.catalog.hotel.exception.UnauthorizedHotelAccessException
 *   com.hotelbook.catalog.user.exception.DuplicateEmailException
 *   com.hotelbook.catalog.user.exception.ResourceNotFoundException
 *   com.hotelbook.catalog.user.exception.InvalidCredentialsException
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@RestControllerAdvice
@Component("hotelGlobalExceptionHandler")
public class GlobalExceptionHandler {

    // ── 404 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHotelNotFound(HotelNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 401 / Auth ────────────────────────────────────────────────────────────

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        // Thrown by Spring Security's AuthenticationManager on bad password
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        // Thrown when isEnabled() = false (isActive = false)
        return error(HttpStatus.UNAUTHORIZED, "Account is disabled");
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        // Thrown when isAccountNonLocked() = false (isActive = false)
        return error(HttpStatus.UNAUTHORIZED, "Account is suspended");
    }

    // ── 403 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler(UnauthorizedHotelAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedHotelAccess(UnauthorizedHotelAccessException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        // Thrown by @PreAuthorize when role check fails
        return error(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    // ── 409 ───────────────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400 — Validation (@Valid failures) ────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 500 — Catch-all ───────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message)
        );
    }

    // ── Inner record for the JSON shape ──────────────────────────────────────

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message
    ) {}

    @ExceptionHandler(LocationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleLocationAlreadyExists(LocationAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFound(LocationNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath().toString()
                        .replaceAll(".*\\.", "")
                        + ": " + v.getMessage())
                .findFirst()
                .orElse("Invalid request parameter");

        return error(HttpStatus.BAD_REQUEST, message);
    }
}
