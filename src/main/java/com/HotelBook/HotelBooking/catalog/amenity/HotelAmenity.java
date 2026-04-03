package com.HotelBook.HotelBooking.catalog.amenity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a single amenity offered by a hotel.
 *
 * Relationship: Many amenities belong to one hotel.
 * No @ManyToOne annotation here because we store hotelId as a plain UUID column
 * (avoids eager-loading the entire Hotel entity on every amenity fetch).
 *
 * Example rows:
 *   ("Outdoor Infinity Pool",  WELLNESS,  "pool")
 *   ("Free High-Speed WiFi",   CONNECTIVITY, "wifi")
 *   ("Complimentary Valet",    PARKING,   "car")
 */
@Entity
@Table(
        name = "hotel_amenities",
        indexes = @Index(name = "idx_amenities_hotel_id", columnList = "hotel_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // FK stored as plain column — no JPA join to avoid loading full Hotel
    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(nullable = false)
    private String name;    // e.g. "Outdoor Swimming Pool", "Fitness Center"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmenityCategory category;

    // Frontend icon key — maps to an icon component in the UI
    // e.g. "pool", "wifi", "gym", "parking", "restaurant"
    // Nullable because the icon can be inferred from category if not specified
    @Column(length = 50)
    private String icon;
}