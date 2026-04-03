package com.HotelBook.HotelBooking.savedhotel;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "saved_hotel",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_saved_hotel_customer_hotel",
                columnNames = {"customer_id", "hotel_id"}
        ),
        indexes = {
                @Index(name = "idx_saved_hotel_customer", columnList = "customer_id"),
                @Index(name = "idx_saved_hotel_hotel",    columnList = "hotel_id")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SavedHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * FK to customer/user — who saved this hotel.
     * Plain UUID — no @ManyToOne (module independence from Member 1).
     */
    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    /**
     * FK to hotel — which hotel was saved.
     * Plain UUID — no @ManyToOne (module independence from Member 1).
     */
    @Column(name = "hotel_id", nullable = false, updatable = false)
    private UUID hotelId;


    @Column(length = 500)
    private String notes;

   @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}