package com.HotelBook.HotelBooking.catalog.hotel;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class HotelResponse {

    private UUID id;
    private String name;
    private HotelType type;
    private String overview;
    private int starRating;

    // Location
    private String address;
    private String city;
    private String countryCode;
    private Double latitude;
    private Double longitude;

    // Contact
    private String phone;
    private String email;
    private String website;

    // Ownership
    private UUID managerId;
    private String managerName;

    // Lifecycle
    private HotelStatus status;
    private String rejectionReason;    // null unless rejected

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
}