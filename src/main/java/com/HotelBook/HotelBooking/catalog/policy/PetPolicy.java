package com.HotelBook.HotelBooking.catalog.policy;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * PetPolicy Entity
 *
 * One-to-one with Hotel. Stored via upsert (create-or-update) — no separate POST vs PUT endpoints.
 * petFee is null when petsAllowed=false (no point charging for something not allowed).
 *
 * Table: pet_policies
 */
@Entity
@Table(
        name = "pet_policies",
        uniqueConstraints = @UniqueConstraint(name = "uq_pet_policy_hotel", columnNames = "hotelId")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID hotelId;

    @Column(nullable = false)
    private boolean petsAllowed;

    // Fee per night for bringing a pet — null if petsAllowed=false
    @Column(precision = 10, scale = 2)
    private BigDecimal petFee;
}
