package com.HotelBook.HotelBooking.search.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SearchRequestDTO {

    // ── REQUIRED ──────────────────────────────────────────────────────────────
    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    // ── GUESTS ────────────────────────────────────────────────────────────────
    @Min(value = 1, message = "At least 1 adult is required")
    private int adults = 1;

    @Min(value = 0, message = "Children cannot be negative")
    private int children = 0;

    // Must match children count exactly when children > 0
    private List<@Min(0) @Max(17) Integer> childrenAges;

    @Min(value = 1, message = "At least 1 room is required")
    private int rooms = 1;

    // ── HOTEL LEVEL FILTERS ───────────────────────────────────────────────────
    // HotelType enum: HOTEL, RESORT, BOUTIQUE, HOSTEL, APARTMENT, VILLA, GUESTHOUSE, MOTEL, INN, LODGE
    private List<String> hotelType;

    // Star rating multiselect e.g. [4, 5]
    private List<@Min(1) @Max(5) Integer> stars;

    @DecimalMin(value = "0.0", message = "Min price cannot be negative")
    private BigDecimal priceMin;

    @DecimalMin(value = "0.0", message = "Max price cannot be negative")
    private BigDecimal priceMax;

    // ── AMENITY FILTERS ───────────────────────────────────────────────────────
    // Amenity names e.g. ["pool", "gym", "wifi"]
    private List<String> hotelAmenities;

    // AmenityCategory enum values e.g. ["WELLNESS", "CONNECTIVITY"]
    private List<String> amenityCategories;

    // ── ROOM LEVEL FILTERS ────────────────────────────────────────────────────
    // RoomType enum: STANDARD, DELUXE, SUITE, STUDIO, FAMILY, VILLA
    private List<String> roomType;

    // BedType enum: KING, QUEEN, TWIN, DOUBLE, SINGLE, BUNK
    private List<String> bedType;

    // RoomView enum: SEA, CITY, GARDEN, POOL, NONE
    private List<String> view;

    // ── POLICY FILTERS ────────────────────────────────────────────────────────
    private Boolean freeCancellation;
    private Boolean breakfastIncluded;
    private Boolean petsAllowed;
    private Boolean wheelchairAccessible;

    // ── SORT & PAGINATION ─────────────────────────────────────────────────────
    // price | stars | name
    private String sortBy = "price";

    // asc | desc
    private String sortOrder = "asc";

    @Min(value = 0)
    private int page = 0;

    @Min(value = 1) @Max(value = 50)
    private int size = 20;
}