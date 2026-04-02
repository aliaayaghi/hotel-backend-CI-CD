package com.HotelBook.HotelBooking.catalog.nearby;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateNearbyPlaceRequest {

    @NotBlank(message = "Place name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @NotNull(message = "Place type is required")
    private NearbyPlaceType type;

    @NotNull(message = "Distance is required")
    @DecimalMin(value = "0.01", message = "Distance must be greater than 0")
    private BigDecimal distanceKm;
}
