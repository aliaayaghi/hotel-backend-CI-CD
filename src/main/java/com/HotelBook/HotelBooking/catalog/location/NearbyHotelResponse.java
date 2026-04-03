package com.HotelBook.HotelBooking.catalog.location;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Lightweight response for proximity search results.
 * Contains just enough info to render a map pin + card.
 */
@Data
@Builder
public class NearbyHotelResponse {

    private UUID hotelId;
    private String hotelName;

    // Location fields
    private String country;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
    private String googleMapsPlaceId;

    /** Straight-line distance in kilometres from the search point */
    private Double distanceKm;
}
