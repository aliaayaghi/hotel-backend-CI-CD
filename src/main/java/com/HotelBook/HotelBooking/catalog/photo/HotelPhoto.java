package com.HotelBook.HotelBooking.catalog.photo;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * HotelPhoto Entity
 *
 * Stores CDN URLs for hotel images.
 * Each hotel can have multiple photos.
 *
 * Cover logic:
 *   - isCover=true marks the thumbnail shown in hotel listing cards.
 *   - display_order determines the sequence in the gallery (0 = first shown).
 *   - Business rule: only ONE photo per hotel can have isCover=true.
 *     HotelPhotoServiceImpl enforces this by clearing any existing cover
 *     before setting a new one.
 *
 * Table: hotel_photos
 */
@Entity
@Table(
        name = "hotel_photos",
        indexes = @Index(name = "idx_hotel_photos_hotel_id", columnList = "hotel_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // FK to hotels.id — stored as a plain UUID column (no @ManyToOne to avoid
    // loading the full Hotel object every time we fetch a photo list).
    @Column(name = "hotel_id", nullable = false, updatable = false)
    private UUID hotelId;

    // Full CDN URL — e.g. "https://cdn.hotelbook.com/hotels/abc/photo1.webp"
    @Column(nullable = false, length = 1000)
    private String url;

    // Optional alt text / accessibility label for the image
    @Column(length = 255)
    private String caption;

    // Zero-based display position. 0 = first in gallery.
    // Managed by reorderPhotos() in the service layer.
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int order = 0;

    // True for the single photo used as the hotel thumbnail in listings.
    // Enforced as unique per hotel by the service (not by a DB constraint
    // because we want to swap the cover atomically without a race condition).
    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean isCover = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant uploadedAt;
}
