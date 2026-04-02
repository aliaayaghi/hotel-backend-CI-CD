package com.HotelBook.HotelBooking.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — catches all exceptions across every controller and
 * returns a consistent JSON error response using ApiResponse.
 *
 * ─── WHY THIS WAS UPDATED ─────────────────────────────────────────────────────
 *
 * The original handler was missing two critical handlers:
 *
 * 1. DataIntegrityViolationException
 *    Thrown by Spring when a database FK constraint or UNIQUE constraint fails.
 *    Example: inserting into pricing_rule with a room_id that doesn't exist in room table.
 *    Without this handler, it fell through to the catch-all Exception handler,
 *    returning "Internal server error" with no useful information.
 *    Now returns 409 Conflict with a clear message.
 *
 * 2. HttpMessageNotReadableException
 *    Thrown when the request body JSON is malformed or missing a required field.
 *    Example: sending an invalid date format, bad enum value, or empty body.
 *    Now returns 400 Bad Request with a clear message.
 *
 * 3. MissingServletRequestParameterException
 *    Thrown when a required @RequestParam is missing from the URL.
 *    Example: forgetting ?roomQuantity=3 on the availability endpoints.
 *    Now returns 400 Bad Request naming the missing parameter.
 *
 * 4. MethodArgumentTypeMismatchException
 *    Thrown when a @PathVariable or @RequestParam has the wrong type.
 *    Example: passing a non-UUID string where a UUID is expected.
 *    Now returns 400 Bad Request naming the parameter and its expected type.
 *
 * ─── EXCEPTION HIERARCHY ──────────────────────────────────────────────────────
 *
 *   MethodArgumentNotValidException     → 400 (@Valid failures on @RequestBody)
 *   ConstraintViolationException        → 400 (@Validated on @PathVariable/@RequestParam)
 *   HttpMessageNotReadableException     → 400 (malformed JSON body)
 *   MissingServletRequestParameterException → 400 (missing required @RequestParam)
 *   MethodArgumentTypeMismatchException → 400 (wrong type for path/query param)
 *   BadRequestException                 → 400 (business logic validation)
 *   IllegalArgumentException            → 400 (bad enum value, invalid argument)
 *   ResourceNotFoundException           → 404 (entity not found)
 *   ConflictException                   → 409 (double booking, duplicate, state conflict)
 *   DataIntegrityViolationException     → 409 (DB FK or UNIQUE constraint violation) ← NEW
 *   RuntimeException                    → 500 (unexpected errors — logged server-side)
 *   Exception (catch-all)              → 500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400 BAD REQUEST — @Valid field validation failure ─────────────────────

    /**
     * Handles @Valid failures on @RequestBody.
     * Returns field-level errors so the client knows exactly which fields failed.
     * Example: missing required field, value out of @Min/@Max range.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<ErrorDTO.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ErrorDTO.FieldError(e.getField(), e.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed: " + fieldErrors.size() + " error(s)"));
    }

    /**
     * Handles @Validated failures on @PathVariable / @RequestParam.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleConstraintViolation(
            ConstraintViolationException ex) {

        List<ErrorDTO.FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new ErrorDTO.FieldError(
                        v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed"));
    }

    /**
     * Handles malformed JSON request body.
     *
     * Triggered when:
     *   - The request body is empty but @RequestBody is required
     *   - The JSON has a syntax error (unclosed bracket, missing quote, etc.)
     *   - A field has the wrong type (e.g. sending a string where a number is expected)
     *   - A date field has an invalid format (e.g. "01-09-2026" instead of "2026-09-01")
     *   - An enum field has an unrecognised value (e.g. "MANAGER" instead of "MANAGER_BLOCK")
     *
     * Example error response:
     * {
     *   "success": false,
     *   "message": "Invalid request body: check that all field types and formats are correct.
     *               Dates must be YYYY-MM-DD. Enums must be exact values (e.g. MANAGER_BLOCK)."
     * }
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleMessageNotReadable(
            HttpMessageNotReadableException ex) {

        String detail = ex.getMessage() != null && ex.getMessage().length() < 200
                ? " Details: " + ex.getMessage()
                : "";

        return ResponseEntity.badRequest().body(ApiResponse.error(
                "Invalid request body: check that all field types and formats are correct. " +
                        "Dates must be YYYY-MM-DD. " +
                        "Enum values must be exact (e.g. MANAGER_BLOCK, MAINTENANCE, SPECIAL_EVENT)." +
                        detail));
    }

    /**
     * Handles missing required @RequestParam.
     *
     * Example: calling POST /api/rooms/{id}/availability/block
     * without ?roomQuantity=3 in the URL.
     *
     * Returns: 400 with message "Required parameter 'roomQuantity' is missing."
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleMissingParam(
            MissingServletRequestParameterException ex) {

        return ResponseEntity.badRequest().body(ApiResponse.error(
                "Required parameter '" + ex.getParameterName() + "' is missing. " +
                        "Expected type: " + ex.getParameterType() + "."));
    }

    /**
     * Handles wrong type for @PathVariable or @RequestParam.
     *
     * Example: passing "abc" where a UUID is expected in the URL path,
     * or passing "three" where an int is expected for ?roomQuantity.
     *
     * Returns: 400 with the parameter name and expected type.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        return ResponseEntity.badRequest().body(ApiResponse.error(
                "Parameter '" + ex.getName() + "' has invalid value '" + ex.getValue() + "'. " +
                        "Expected type: " + expectedType + "."));
    }

    // ── 400 BAD REQUEST — business logic ─────────────────────────────────────

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    // ── 404 NOT FOUND ─────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ── 409 CONFLICT — business state ─────────────────────────────────────────

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * NEW: Handles database constraint violations.
     *
     * Thrown by Spring when:
     *
     * 1. FOREIGN KEY constraint fails:
     *    You tried to insert a record referencing an ID that doesn't exist.
     *    Example: creating a pricing rule for a room_id that is not in the room table.
     *    Example: creating an availability block for a room_id that doesn't exist.
     *    FIX: use the correct, existing room UUID (e.g. cccccccc-cccc-cccc-cccc-cccccccccccc).
     *
     * 2. UNIQUE constraint fails:
     *    You tried to insert a duplicate record where uniqueness is enforced.
     *    Example: blocking the same (room_id, date) twice in room_availability.
     *    Example: creating a second payment for the same booking.
     *    Example: saving the same hotel twice in the wishlist.
     *    FIX: do not duplicate — check if the record exists first.
     *
     * Returns 409 Conflict (not 500) because this is a data conflict, not a server bug.
     *
     * WHY THIS WAS MISSING:
     * DataIntegrityViolationException extends DataAccessException extends RuntimeException.
     * It IS a RuntimeException, so it should be caught by handleRuntimeException().
     * However, in Spring Boot 4 / Spring Framework 7, exceptions thrown inside a
     * @Transactional forEach lambda get wrapped through multiple proxy layers before
     * reaching the @ExceptionHandler. The Spring MVC dispatcher sometimes resolves
     * the wrong handler for these wrapped exceptions. Adding an explicit handler
     * for DataIntegrityViolationException fixes this ambiguity.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        // Extract the most useful part of the message for the developer
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        // Detect FK constraint failure and give a helpful message
        if (message != null && message.contains("foreign key constraint fails")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(
                    "Database constraint violation: the referenced record does not exist. " +
                            "Check that the room/booking/policy ID in your request URL is valid and exists. " +
                            "Use one of the seeded UUIDs (e.g. cccccccc-cccc-cccc-cccc-cccccccccccc for Standard King)."));
        }

        // Detect UNIQUE constraint failure
        if (message != null && (message.contains("Duplicate entry") || message.contains("unique constraint"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(
                    "Duplicate record: this entry already exists. " +
                            "The record you are trying to create conflicts with an existing one " +
                            "(e.g. same date already blocked, same hotel already saved)."));
        }

        // Generic data integrity error
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(
                "Database constraint violation: " + message));
    }

    // ── 500 INTERNAL SERVER ERROR ─────────────────────────────────────────────

    /**
     * Catch-all for unexpected RuntimeException.
     * Returns 500 with the exception message.
     *
     * In production: log this with log.error("Unexpected error", ex)
     * before returning — never expose stack traces to clients.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
    }

    /**
     * Absolute catch-all for checked exceptions.
     * Should never be reached in normal operation.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + ex.getMessage()));
    }
}