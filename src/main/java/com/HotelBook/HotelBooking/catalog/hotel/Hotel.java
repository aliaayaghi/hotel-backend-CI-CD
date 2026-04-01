package com.HotelBook.HotelBooking.catalog.hotel;

import com.HotelBook.HotelBooking.catalog.user.entity.HotelManager;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "hotels",
        indexes = {
                @Index(name = "idx_hotels_manager_id",  columnList = "manager_id"),
                @Index(name = "idx_hotels_status",       columnList = "status"),
                @Index(name = "idx_hotels_city_country", columnList = "city, country_code")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ── Ownership ────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private HotelManager manager;

    // ── Identity ─────────────────────────────────────────────────────────────
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HotelType type = HotelType.HOTEL;

    @Column(columnDefinition = "TEXT")
    private String overview;             // long description

    // ── Star rating 1–5 ──────────────────────────────────────────────────────
    @Column(nullable = false)
    @Builder.Default
    private int starRating = 3;

    // ── Location ─────────────────────────────────────────────────────────────
    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 2)
    private String countryCode;          // ISO 3166-1 alpha-2  e.g. "PS", "JO"

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // ── Contact ──────────────────────────────────────────────────────────────
    @Column(length = 20)
    private String phone;

    @Column
    private String email;

    @Column
    private String website;

    // ── Status / lifecycle ────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HotelStatus status = HotelStatus.PENDING;

    // Populated by admin when rejecting; null otherwise
    @Column(length = 500)
    private String rejectionReason;

    // ── Audit ─────────────────────────────────────────────────────────────────
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
