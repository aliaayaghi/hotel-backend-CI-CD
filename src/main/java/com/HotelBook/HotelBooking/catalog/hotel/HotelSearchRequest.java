package com.HotelBook.HotelBooking.catalog.hotel;

import lombok.Data;

@Data
public class HotelSearchRequest {

    // Free-text — matches against name, city, country, address
    private String keyword;

    private String city;

    // ISO 3166-1 alpha-2 e.g. "PS"
    private String countryCode;

    private HotelType type;

    // 1–5 star filter; null = no filter
    private Integer starRating;
}
