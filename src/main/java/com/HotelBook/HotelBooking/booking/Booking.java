package com.HotelBook.HotelBooking.booking;
import com.HotelBook.HotelBooking.payment.Payment;
import com.HotelBook.HotelBooking.room.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "booking",
        indexes = {
                @Index(name = "idx_booking_customer",   columnList = "customer_id"),
                @Index(name = "idx_booking_room",       columnList = "room_id"),
                @Index(name = "idx_booking_hotel",      columnList = "hotel_id"),
                @Index(name = "idx_booking_status",     columnList = "status"),
                @Index(name = "idx_booking_check_in",   columnList = "check_in_date"),
                @Index(name = "idx_booking_check_out",  columnList = "check_out_date")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id", referencedColumnName = "booking_id",
            table = "payment", insertable = false, updatable = false)
    private Payment payment;


    /** FK to customer/user (Member 1's entity). Plain UUID — no @ManyToOne. */
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /** FK to hotel (Member 1's entity). Stored for quick lookup without joining Room. */
    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    /** FK to room.id — the specific room type being booked. */
    @Column(name = "room_id", nullable = false)
    private UUID roomId;

   @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;


    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;


    @Column(name = "adults", nullable = false)
    private Integer adults;

    @Column(name = "children", nullable = false)
    @Builder.Default
    private Integer children = 0;


    @Column(name = "room_count", nullable = false)
    @Builder.Default
    private Integer roomCount = 1;


    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;


    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;


    @Column(name = "cancellation_policy_id")
    private UUID cancellationPolicyId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

   @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

   @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

   @Column(name = "cancelled_by", length = 20)
    private String cancelledBy;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public long getNumberOfNights() {
        return checkInDate.until(checkOutDate).getDays();
    }

   public boolean isCancellable() {
        return status == BookingStatus.PENDING || status == BookingStatus.CONFIRMED;
    }

   public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }
}
