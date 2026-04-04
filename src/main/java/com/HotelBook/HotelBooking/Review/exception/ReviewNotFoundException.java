package com.HotelBook.HotelBooking.Review.exception;


import com.HotelBook.HotelBooking.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends BusinessException {
    public ReviewNotFoundException(Long id) {
        super("REVIEW_NOT_FOUND",
                "Review with id " + id + " was not found",
                HttpStatus.NOT_FOUND);
    }
}