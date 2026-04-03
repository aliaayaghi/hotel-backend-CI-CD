package com.HotelBook.HotelBooking.pricing;


import com.HotelBook.HotelBooking.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(
        name = "pricing_rule",
        indexes = {
                @Index(name = "idx_pricing_room_id", columnList = "room_id"),
                @Index(name = "idx_pricing_active",  columnList = "is_active"),
                @Index(name = "idx_pricing_room_active", columnList = "room_id, is_active")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * FK to room.id — plain UUID (not @ManyToOne) for module independence.
     * One room can have many pricing rules (one-to-many relationship).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;



    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    /** Which type of rule this is — determines which fields are used. */
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30)
    private RuleType ruleType;

    /**
     * Start date for SEASONAL and SPECIAL_EVENT rules.
     * NULL for WEEKDAY_WEEKEND rules (they repeat every week with no date boundary).
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * End date for SEASONAL and SPECIAL_EVENT rules (inclusive).
     * NULL for WEEKDAY_WEEKEND rules.
     */
    @Column(name = "end_date")
    private LocalDate endDate;


    @Column(name = "day_of_week", length = 100)
    private String dayOfWeek;


    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal multiplier;


    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;


    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Optional human-readable note. e.g. "Summer peak season 2026" */
    @Column(length = 255)
    private String description;

    // ── ENUMS ─────────────────────────────────────────────────────────────────


    public enum RuleType {

        WEEKDAY_WEEKEND,

        SEASONAL,

        SPECIAL_EVENT
    }


    public boolean isApplicableOnDate(LocalDate date) {
        // Disabled rules never apply
        if (!Boolean.TRUE.equals(isActive)) return false;

        switch (ruleType) {

            case SEASONAL:
            case SPECIAL_EVENT:
                // Requires startDate and endDate — null means misconfigured rule
                if (startDate == null || endDate == null) return false;
                // Inclusive on both ends: startDate <= date <= endDate
                return !date.isBefore(startDate) && !date.isAfter(endDate);

            case WEEKDAY_WEEKEND:
                // Requires the dayOfWeek CSV field
                if (dayOfWeek == null || dayOfWeek.isBlank()) return false;
                // date.getDayOfWeek().name() returns "MONDAY", "FRIDAY", etc.
                String dayName = date.getDayOfWeek().name(); // e.g. "FRIDAY"
                // Check if "FRIDAY" appears in "FRIDAY,SATURDAY"
                return dayOfWeek.toUpperCase().contains(dayName);

            default:
                return false;
        }
    }
}
