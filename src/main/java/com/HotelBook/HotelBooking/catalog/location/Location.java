package com.HotelBook.HotelBooking.catalog.location;


import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Location Entity
 *
 * One-to-one with Hotel — every hotel has exactly one location record.
 * Stores full address, coordinates (lat/lng for map display), and
 * optional Google Maps Place ID for deep linking.
 *
 * ┌──────────────────────────────────────────────────────────┐
 * │  locations table                                          │
 * │  id (FK → hotels.id, PK via @MapsId)                     │
 * │  country, city, state, address, zip_code                 │
 * │  latitude, longitude                                     │
 * │  google_maps_place_id                                    │
 * └──────────────────────────────────────────────────────────┘
 */
@Entity
@Table(
        name = "locations",
        indexes = {
                @Index(name = "idx_location_city",    columnList = "city"),
                @Index(name = "idx_location_country", columnList = "country"),
                @Index(name = "idx_location_latlng",  columnList = "latitude, longitude")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    /** Shares Hotel's UUID — no separate key generated */
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Hotel hotel;

    // ── Address ──────────────────────────────────────────────────────────────

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    /** State / province / region — optional */
    @Column(length = 100)
    private String state;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 20)
    private String zipCode;

    // ── Coordinates ──────────────────────────────────────────────────────────

    /** Latitude  — WGS84, e.g.  31.9539 */
    @Column(nullable = false)
    private Double latitude;

    /** Longitude — WGS84, e.g.  35.9106 */
    @Column(nullable = false)
    private Double longitude;

    // ── Optional deep-link ────────────────────────────────────────────────────

    /** Google Maps Place ID — allows frontend to embed a precise map pin */
    @Column(length = 255)
    private String googleMapsPlaceId;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}