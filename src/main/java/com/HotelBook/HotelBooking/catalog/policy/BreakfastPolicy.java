package com.HotelBook.HotelBooking.catalog.policy;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * BreakfastPolicy Entity
 *
 * One-to-one with Hotel. Uses upsert pattern (same as PetPolicy).
 *
 * Logical rules:
 *   - breakfastOffered=false → includedInPrice=false, pricePerPerson=null, type=null
 *   - includedInPrice=true → pricePerPerson=null (it's free)
 *
 * These rules are enforced in BreakfastPolicyServiceImpl, not at DB level.
 *
 * Table: breakfast_policies
 */
@Entity
@Table(
        name = "breakfast_policies",
        uniqueConstraints = @UniqueConstraint(name = "uq_breakfast_policy_hotel", columnNames = "hotelId")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakfastPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID hotelId;

    @Column(nullable = false)
    private boolean breakfastOffered;

    // true = breakfast is included in the room price (free)
    @Column(nullable = false)
    @Builder.Default
    private boolean includedInPrice = false;

    // Charged per person per night — null if included or not offered
    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerPerson;

    // null if breakfastOffered=false
    @Enumerated(EnumType.STRING)
    @Column
    private BreakfastType type;
}