package com.HotelBook.HotelBooking.catalog.location;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LocationResponse {

    private UUID hotelId;
    private String country;
    private String city;
    private String state;
    private String address;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private String googleMapsPlaceId;
    private Instant updatedAt;
}
