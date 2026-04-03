package com.HotelBook.HotelBooking.catalog.user.exception;

/**
 * Thrown when an authenticated user attempts to perform an action
 * on a resource they do not own.
 * Example: A HotelManager trying to modify another manager's hotel photos.
 * M3's GlobalExceptionHandler should map this to HTTP 403 Forbidden.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
