package com.HotelBook.HotelBooking.catalog.amenity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAmenityRequest {

    @NotBlank(message = "Amenity name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Amenity category is required")
    private AmenityCategory category;

    // Optional — frontend icon key (e.g. "pool", "wifi", "gym")
    // If null, the frontend will infer the icon from the category
    @Size(max = 50, message = "Icon key must be 50 characters or fewer")
    private String icon;
}
