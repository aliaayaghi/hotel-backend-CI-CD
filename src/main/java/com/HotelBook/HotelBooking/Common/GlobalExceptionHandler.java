package com.HotelBook.HotelBooking.Common;

import com.HotelBook.HotelBooking.Common.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — single unified handler for the entire project.
 *
 * Merged from:
 *   M1  com.HotelBook.HotelBooking.GlobalExceptionHandler
 *   M2  com.HotelBook.HotelBooking.Common.GlobalExceptionHandler
 *   M3  com.HotelBook.HotelBooking.Common.exception.GlobalExceptionHandler
 *
 * Canonical location: com.HotelBook.HotelBooking.common.GlobalExceptionHandler
 * Delete the other three files after this is committed.
 *
 * ── RESPONSE SHAPE ────────────────────────────────────────────────────────────
 * Every endpoint returns the same ErrorResponse record:
 * {
 *   "timestamp": "2026-04-05T10:00:00Z",
 *   "status":    409,
 *   "error":     "Conflict",
 *   "message":   "Hotel is already in your wishlist.",
 *   "fieldErrors": {}          // only present on validation failures
 * }
 *
 * ── STATUS CODE MAP ───────────────────────────────────────────────────────────
 *   400  MethodArgumentNotValidException       @Valid body failures
 *   400  ConstraintViolationException          @Validated path/query param failures
 *   400  HttpMessageNotReadableException       malformed JSON, bad enum, bad date
 *   400  MissingServletRequestParameterException  missing required @RequestParam
 *   400  MethodArgumentTypeMismatchException   wrong type for path/query param
 *   400  IllegalArgumentException              bad argument in business logic
 *   401  InvalidCredentialsException           wrong email or password
 *   401  BadCredentialsException               Spring Security auth failure
 *   401  DisabledException                     account disabled
 *   401  LockedException                       account suspended
 *   403  AccessDeniedException                 missing role / @PreAuthorize failed
 *   403  AuthorizationDeniedException          Spring Security 6 authorization failure
 *   403  UnauthorizedHotelAccessException      manager editing another manager's hotel
 *   404  ResourceNotFoundException             any entity not found (user, hotel, etc.)
 *   404  HotelNotFoundException                hotel-specific not found
 *   404  LocationNotFoundException             location not found
 *   404  ReviewNotFoundException               review not found
 *   404  NoResourceFoundException              endpoint URL does not exist
 *   409  ConflictException                     business state conflict (double-save, etc.)
 *   409  DuplicateEmailException               email already registered
 *   409  LocationAlreadyExistsException        hotel already has a location
 *   409  DataIntegrityViolationException       DB FK or UNIQUE constraint violated
 *   500  Exception (catch-all)                 anything unexpected
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ══════════════════════════════════════════════════════════════════════════
    // 400 — VALIDATION
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * @Valid failures on @RequestBody — returns field-level error map.
     * Used by every controller that accepts a DTO body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        return build(HttpStatus.BAD_REQUEST,
                "Validation failed: " + fieldErrors.size() + " error(s)",
                fieldErrors);
    }

    /**
     * @Validated failures on @PathVariable / @RequestParam.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> fieldErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString().replaceAll(".*\\.", ""),
                        v -> v.getMessage(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    /**
     * Malformed JSON body, bad enum value, bad date format, missing body.
     * Example: sending "01-09-2026" where "2026-09-01" is required.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex) {

        String detail = (ex.getMessage() != null && ex.getMessage().length() < 200)
                ? " Detail: " + ex.getMessage()
                : "";

        return build(HttpStatus.BAD_REQUEST,
                "Invalid request body: check that all field types and formats are correct. " +
                        "Dates must be YYYY-MM-DD. Enum values must be exact (e.g. MANAGER_BLOCK)." + detail,
                null);
    }

    /**
     * Missing required @RequestParam.
     * Example: calling an endpoint without ?roomQuantity=3.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex) {

        return build(HttpStatus.BAD_REQUEST,
                "Required parameter '" + ex.getParameterName() + "' is missing. " +
                        "Expected type: " + ex.getParameterType() + ".",
                null);
    }

    /**
     * Wrong type for @PathVariable or @RequestParam.
     * Example: passing "abc" where a UUID is expected.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String expected = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName() : "unknown";

        return build(HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' has invalid value '" + ex.getValue() +
                        "'. Expected type: " + expected + ".",
                null);
    }

    /**
     * Explicit bad argument thrown from service/business logic.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return build(ex.getHttpStatus(), ex.getMessage(), null);
    }
    // ══════════════════════════════════════════════════════════════════════════
    // 401 — AUTHENTICATION
    // ══════════════════════════════════════════════════════════════════════════

    /** Wrong email or password from business layer. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    /** Spring Security failed to authenticate (bad password, user not found). */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", null);
    }

    /** Account has been disabled (isEnabled = false). */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Account is disabled", null);
    }

    /** Account has been suspended (isAccountNonLocked = false). */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Account is suspended", null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 403 — AUTHORIZATION
    // ══════════════════════════════════════════════════════════════════════════

    /** @PreAuthorize or hasRole() check failed. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action", null);
    }

    /** Spring Security 6 method security authorization failure. */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Access denied — insufficient role", null);
    }

    /** Manager tried to edit a hotel they do not own. */
    @ExceptionHandler(UnauthorizedHotelAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedHotelAccess(
            UnauthorizedHotelAccessException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }


    // ══════════════════════════════════════════════════════════════════════════
    // 404 — NOT FOUND
    // ══════════════════════════════════════════════════════════════════════════

    /** Generic entity not found — covers user, booking, payment, notification, etc. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHotelNotFound(HotelNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFound(LocationNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReviewNotFound(ReviewNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /** The URL path itself doesn't match any endpoint. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEndpoint(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND,
                "No route found for '" + ex.getHttpMethod() + " " + ex.getResourcePath() +
                        "'. Please check the URL and HTTP method.", null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 409 — CONFLICT
    // ══════════════════════════════════════════════════════════════════════════

    /** Business-level state conflict (double-save wishlist, booking overlap, etc.). */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }


    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }


    /** Email already registered during sign-up. */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /** Hotel already has a location (one-to-one constraint). */
    @ExceptionHandler(LocationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleLocationAlreadyExists(
            LocationAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /**
     * Database FK or UNIQUE constraint violation.
     *
     * FK example:   inserting a pricing rule for a room_id that doesn't exist.
     * UNIQUE example: saving the same hotel twice in the wishlist,
     *                 or blocking the same (room_id, date) twice.
     *
     * Returns 409, not 500, because this is a data conflict not a server bug.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        String root = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        if (root != null && root.contains("foreign key constraint fails")) {
            return build(HttpStatus.CONFLICT,
                    "The referenced record does not exist. " +
                            "Check that the ID in your request URL is valid and exists in the database.",
                    null);
        }

        if (root != null && (root.contains("Duplicate entry")
                || root.contains("unique constraint"))) {
            return build(HttpStatus.CONFLICT,
                    "Duplicate record: this entry already exists " +
                            "(e.g. same date already blocked, same hotel already saved).",
                    null);
        }

        log.warn("Unrecognized data integrity violation: {}", root);
        return build(HttpStatus.CONFLICT,
                "A data conflict occurred. Please check your request and try again.", null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 500 — UNEXPECTED
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Absolute catch-all.
     * Always log the stack trace server-side — never expose it to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again.", null);
    }


    // ══════════════════════════════════════════════════════════════════════════
    // SHARED RESPONSE RECORD
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Unified error response shape used by every handler in this class.
     * fieldErrors is null/omitted for non-validation errors.
     */
    public record ErrorResponse(
            Instant             timestamp,
            int                 status,
            String              error,
            String              message,
            Map<String, String> fieldErrors
    ) {}

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, Map<String, String> fieldErrors) {

        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                fieldErrors
        ));
    }
}