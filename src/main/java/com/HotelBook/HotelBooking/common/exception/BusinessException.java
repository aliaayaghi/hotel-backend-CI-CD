package com.HotelBook.HotelBooking.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    // ── COMMON FACTORY METHODS ────────────────────────────────────────────────

    public static BusinessException notFound(String entity, Long id) {
        return new BusinessException(
                entity.toUpperCase() + "_NOT_FOUND",
                entity + " with id " + id + " was not found",
                HttpStatus.NOT_FOUND
        );
    }

    public static BusinessException conflict(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.CONFLICT);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public static BusinessException badRequest(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.BAD_REQUEST);
    }


}