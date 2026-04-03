package com.HotelBook.HotelBooking.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDTO {

    private String error;                    // error code  e.g. "REVIEW_NOT_FOUND"
    private String message;                  // human readable e.g. "Review with id 5 was not found"
    private Map<String, String> fieldErrors; // only populated on validation errors
    private LocalDateTime timestamp;

    // ── FACTORY METHODS ───────────────────────────────────────────────────────

    // Use this for simple errors — not found, forbidden, conflict etc.
    public static ErrorDTO of(String error, String message) {
        return ErrorDTO.builder()
                .error(error)
                .message(message)
                .fieldErrors(null)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Use this only for validation errors where you want to show field-level detail
    public static ErrorDTO withFieldErrors(String error,
                                           String message,
                                           Map<String, String> fieldErrors) {
        return ErrorDTO.builder()
                .error(error)
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}