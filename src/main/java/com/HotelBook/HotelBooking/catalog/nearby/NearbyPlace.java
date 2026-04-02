package com.HotelBook.HotelBooking.catalog.nearby;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a point of interest near a hotel.
 *
 * Examples:
 *   ("Dubai International Airport", AIRPORT,    12.50 km)
 *   ("JBR Beach",                   BEACH,       0.80 km)
 *   ("Dubai Mall",                  MALL,        5.20 km)
 *
 * Multiple nearby places can exist for a single hotel.
 * Stored as plain hotelId UUID — no JPA join to avoid loading the full Hotel.
 */
@Entity
@Table(
        name = "nearby_places",
        indexes = @Index(name = "idx_nearby_places_hotel_id", columnList = "hotel_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // FK stored as plain UUID column — no eager-loading Hotel
    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(nullable = false)
    private String name;    // human-readable place name

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NearbyPlaceType type;

    // Distance in kilometers, e.g. 2.30
    // Precision 5, scale 2 supports distances up to 999.99 km
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal distanceKm;
}