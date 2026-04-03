package com.HotelBook.HotelBooking.catalog.policy;


/**
 * Thrown when a client tries to CREATE a resource that already exists.
 *
 * Examples:
 *   - Hotel already has a Location → POST /api/hotels/{id}/location
 *   - Hotel already has a CheckInPolicy → POST /api/hotels/{id}/policies/checkin
 *
 * M3 maps this to HTTP 409 Conflict in GlobalExceptionHandler:
 *   @ExceptionHandler(ConflictException.class)
 *   public ResponseEntity<ErrorDTO> handle(...) { return ResponseEntity.status(409)... }
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
