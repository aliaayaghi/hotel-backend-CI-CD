package com.HotelBook.HotelBooking.payment;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(
        name = "payment",
        indexes = {
                @Index(name = "idx_payment_booking_id",  columnList = "booking_id",  unique = true),
                @Index(name = "idx_payment_customer_id", columnList = "customer_id"),
                @Index(name = "idx_payment_status",      columnList = "status")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;


    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;


    @Column(name = "customer_id", nullable = false)
    private UUID customerId;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;


    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PAID;


    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;


    @Column(name = "paid_at")
    private LocalDateTime paidAt;


    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;


    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
