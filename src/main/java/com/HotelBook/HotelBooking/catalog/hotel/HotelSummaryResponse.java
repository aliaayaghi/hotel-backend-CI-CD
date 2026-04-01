package com.HotelBook.HotelBooking.catalog.hotel;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Lightweight projection used in search results and paginated lists.
 * Does not include overview text, contact info, or manager details —
 * those are in HotelResponse (the full detail endpoint).
 */
@Data
@Builder
public class HotelSummaryResponse {

    private UUID id;
    private String name;
    private HotelType type;
    private int starRating;
    private String city;
    private String countryCode;
    private String address;
    private Double latitude;
    private Double longitude;
}
