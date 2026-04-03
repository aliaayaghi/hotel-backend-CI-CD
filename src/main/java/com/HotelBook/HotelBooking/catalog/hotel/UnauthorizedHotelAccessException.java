package com.HotelBook.HotelBooking.catalog.hotel;

import java.util.UUID;

public class UnauthorizedHotelAccessException extends RuntimeException {

    public UnauthorizedHotelAccessException(UUID hotelId) {
        super("You do not have permission to access hotel: " + hotelId);
    }
}