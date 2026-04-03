package com.HotelBook.HotelBooking.catalog.user.exception;


public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        // Generic message — never reveal whether it was the email or password
        super("Invalid email or password");
    }
}

