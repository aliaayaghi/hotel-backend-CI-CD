package com.HotelBook.HotelBooking.roomavailability;



import com.HotelBook.HotelBooking.room.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "room_availability",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_room_availability_room_date",
                columnNames = {"room_id", "date"}
        ),
        indexes = {
                @Index(name = "idx_avail_room_id",   columnList = "room_id"),
                @Index(name = "idx_avail_date",      columnList = "date"),
                @Index(name = "idx_avail_room_date", columnList = "room_id, date"),
                @Index(name = "idx_avail_booking",   columnList = "booking_id")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * FK to room.id — stored as plain UUID (not @ManyToOne).
     * WHY: keeps this module independent of the Room entity's package.
     * We can still query by roomId without needing to join the Room table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;


    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    /**
     * The calendar date that is blocked.
     * One row per date — never one row for a range.
     * This makes date-range queries fast: WHERE date BETWEEN :checkIn AND :checkOut-1
     */
    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "blocked_count", nullable = false)
    @Builder.Default
    private Integer blockedCount = 1;

    /**
     * Why this date is blocked.
     * BOOKING       → a customer made a reservation
     * MAINTENANCE   → hotel blocking for cleaning / repair
     * MANAGER_BLOCK → manual override by hotel manager
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "blocked_reason", nullable = false, length = 30)
    private BlockedReason blockedReason;

    /**
     * FK to booking.id — populated when blockedReason=BOOKING.
     * NULL for MAINTENANCE and MANAGER_BLOCK (no booking associated).
     * Used by unblockDates() when a booking is cancelled:
     *   DELETE FROM room_availability WHERE booking_id = :cancelledBookingId
     */
    @Column(name = "booking_id")
    private UUID bookingId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    public enum BlockedReason {
        /** A confirmed booking is occupying this room on this date. */
        BOOKING,

        /** Hotel is performing maintenance (cleaning, repairs, renovations). */
        MAINTENANCE,

        /** Hotel manager manually blocked this date for any reason. */
        MANAGER_BLOCK
    }
}
