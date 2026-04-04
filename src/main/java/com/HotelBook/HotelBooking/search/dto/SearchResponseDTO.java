package com.HotelBook.HotelBooking.search.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SearchResponseDTO {

    private UUID id;
    private String name;
    private int starRating;
    private String type;

    // Location — from Hotel entity directly (city, countryCode, address, lat, lng)
    private String city;
    private String countryCode;
    private String address;
    private Double latitude;
    private Double longitude;

    // Pricing
    private BigDecimal lowestPrice;

    // Availability summary
    private int availableRooms;

    // Top 5 amenities for card display
    private List<String> topAmenities;

    // Policy highlights
    private boolean freeCancellationAvailable;
    private boolean breakfastIncluded;
    private boolean petsAllowed;
}