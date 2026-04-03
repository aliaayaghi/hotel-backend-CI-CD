package com.HotelBook.HotelBooking.catalog.hotel;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateHotelRequest {

    // All fields optional — only non-null values will be applied

    @Size(min = 2, max = 150, message = "Hotel name must be between 2 and 150 characters")
    private String name;

    private HotelType type;

    @Size(max = 3000, message = "Overview must be at most 3000 characters")
    private String overview;

    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating must be at most 5")
    private Integer starRating;

    @Size(min = 2, max = 150)
    private String address;

    @Size(min = 2, max = 100)
    private String city;

    @Size(min = 2, max = 2, message = "Country code must be a 2-letter ISO code")
    private String countryCode;

    private Double latitude;
    private Double longitude;

    @Size(max = 20)
    private String phone;

    @Email(message = "Hotel email must be a valid email address")
    private String email;

    @Size(max = 255)
    private String website;
}