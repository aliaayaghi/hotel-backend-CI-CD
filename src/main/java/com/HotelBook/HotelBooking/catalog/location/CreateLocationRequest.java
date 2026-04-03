package com.HotelBook.HotelBooking.catalog.location;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateLocationRequest {

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String zipCode;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0",  message = "Latitude must be >= -90")
    @DecimalMax(value =  "90.0",  message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value =  "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @Size(max = 255)
    private String googleMapsPlaceId;
}
