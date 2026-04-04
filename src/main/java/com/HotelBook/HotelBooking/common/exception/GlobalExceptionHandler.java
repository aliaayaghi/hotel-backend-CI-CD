package com.HotelBook.HotelBooking.common.exception;

//import com.HotelBook.HotelBooking.Review.exception.ReviewNotFoundException;
import com.HotelBook.HotelBooking.common.dto.ErrorDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Component("commonExceptionHandler")
public class GlobalExceptionHandler {

    // ── YOUR CUSTOM EXCEPTIONS ────────────────────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDTO> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ErrorDTO.of(ex.getErrorCode(), ex.getMessage()));
    }

    // Handles your existing ReviewNotFoundException etc.
//    @ExceptionHandler({ReviewNotFoundException.class})
//    public ResponseEntity<ErrorDTO> handleReviewNotFound(RuntimeException ex) {
//        return ResponseEntity
//                .status(HttpStatus.NOT_FOUND)
//                .body(ErrorDTO.of("REVIEW_NOT_FOUND", ex.getMessage()));
//    }

    // ── VALIDATION ERRORS (400) ───────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDTO.withFieldErrors(
                        "VALIDATION_FAILED",
                        "One or more fields are invalid",
                        fieldErrors
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(v -> fieldErrors.put(
                        v.getPropertyPath().toString(),
                        v.getMessage()
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDTO.withFieldErrors(
                        "VALIDATION_FAILED",
                        "One or more fields are invalid",
                        fieldErrors
                ));
    }

    // ── ILLEGAL ARGUMENT (400) ────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDTO.of("BAD_REQUEST", ex.getMessage()));
    }

    // ── ACCESS DENIED (403) ───────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorDTO.of("FORBIDDEN", "You don't have permission to do this"));
    }

    // ── NOT FOUND (404) ───────────────────────────────────────────────────────

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDTO> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorDTO.of("ENDPOINT_NOT_FOUND",
                        "The requested endpoint does not exist"));
    }

    // ── CATCH ALL (500) ───────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleAllOther(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDTO.of("INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again."));
    }
}