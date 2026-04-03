package com.HotelBook.catalog.location;

import java.util.UUID;

public class LocationAlreadyExistsException extends RuntimeException {

    public LocationAlreadyExistsException(UUID hotelId) {
        super("A location record already exists for hotel: " + hotelId);
    }
}
