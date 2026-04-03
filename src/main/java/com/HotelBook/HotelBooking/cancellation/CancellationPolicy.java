package com.HotelBook.HotelBooking.cancellation;



import com.HotelBook.HotelBooking.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Entity
@Table(
        name = "cancellation_policy",
        indexes = {
                @Index(name = "idx_cancel_room_id",   columnList = "room_id"),
                @Index(name = "idx_cancel_hotel_id",  columnList = "hotel_id"),
                @Index(name = "idx_cancel_is_default", columnList = "is_default")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CancellationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;


    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;


    @Column(name = "room_id")
    private UUID roomId;


    @Column(name = "tier_name", nullable = false, length = 100)
    private String tierName;


    @Column(name = "deadline_hours", nullable = false)
    private Integer deadlineHours;


    @Column(name = "refund_percentage", nullable = false)
    private Integer refundPercentage;


    @Column(name = "price_multiplier", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal priceMultiplier = BigDecimal.ONE;


    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    /** Optional detailed explanation shown to customers. */
    @Column(columnDefinition = "TEXT")
    private String description;


    public BigDecimal calculateRefund(LocalDateTime cancelledAt,
                                      LocalDateTime checkInDateTime,
                                      BigDecimal amountPaid) {

        // Non-refundable: always 0, skip the time check
        if (deadlineHours == 0) {
            return BigDecimal.ZERO;
        }

        long hoursUntilCheckIn = ChronoUnit.HOURS.between(cancelledAt, checkInDateTime);

        if (hoursUntilCheckIn >= deadlineHours) {
            // Customer cancelled early enough — apply the refund percentage
            return amountPaid
                    .multiply(BigDecimal.valueOf(refundPercentage))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // Too late to cancel with a refund
            return BigDecimal.ZERO;
        }
    }


    public BigDecimal getAdjustedPrice(BigDecimal basePrice) {
        return basePrice.multiply(priceMultiplier).setScale(2, RoundingMode.HALF_UP);
    }


    public boolean isEligibleForRefund(LocalDateTime cancelledAt,
                                       LocalDateTime checkInDateTime) {
        if (deadlineHours == 0) return false; // Non-refundable — never eligible

        long hoursUntilCheckIn = ChronoUnit.HOURS.between(cancelledAt, checkInDateTime);
        return hoursUntilCheckIn >= deadlineHours;
    }


    public boolean isNonRefundable() {
        return deadlineHours == 0 && refundPercentage == 0;
    }
}
