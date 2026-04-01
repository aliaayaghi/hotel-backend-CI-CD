package com.HotelBook.HotelBooking.catalog.hotel;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateHotelRequest {

    @NotBlank(message = "Hotel name is required")
    @Size(min = 2, max = 150, message = "Hotel name must be between 2 and 150 characters")
    private String name;

    private HotelType type = HotelType.HOTEL;

    @Size(max = 3000, message = "Overview must be at most 3000 characters")
    private String overview;

    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating must be at most 5")
    private int starRating = 3;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 2, message = "Country code must be a 2-letter ISO code")
    private String countryCode;

    private Double latitude;
    private Double longitude;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Email(message = "Hotel email must be a valid email address")
    private String email;

    @Size(max = 255, message = "Website URL must be at most 255 characters")
    private String website;
}
