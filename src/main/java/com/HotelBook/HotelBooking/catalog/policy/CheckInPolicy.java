package com.HotelBook.HotelBooking.catalog.policy;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * CheckInPolicy Entity
 *
 * One-to-one with Hotel — unique constraint on hotelId ensures only one policy per hotel.
 * Stores check-in window times and early/late availability flags.
 *
 * Table: checkin_policies
 */
@Entity
@Table(
        name = "checkin_policies",
        uniqueConstraints = @UniqueConstraint(name = "uq_checkin_hotel", columnNames = "hotelId")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Unique FK → hotels.id — enforces one policy per hotel
    @Column(unique = true, nullable = false)
    private UUID hotelId;

    // e.g. "14:00" — time from which guests can check in
    @Column
    private String earliestTime;

    // e.g. "23:59" — last time to check in before the next day
    @Column
    private String latestTime;

    // true = early check-in is available (possibly for a fee)
    @Column
    private boolean earlyCheckIn;

    // true = late check-out is available (possibly for a fee)
    @Column
    private boolean lateCheckOut;

    // Free-text additional policy notes
    @Column(columnDefinition = "TEXT")
    private String description;
}
