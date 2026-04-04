package com.HotelBook.HotelBooking.notification;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.Instant;
import java.util.UUID;

/**
 * Notification Entity
 *
 * Every email (or future SMS/push) sent by the system is persisted here.
 * This serves as:
 *   1. An audit trail for support ("Did the confirmation email go out?")
 *   2. The data source for GET /api/customers/me/notifications
 *   3. A retry queue if we add a scheduled job for FAILED notifications
 *
 * ── CRITICAL: The notification row is ALWAYS saved, even when sending fails.
 * Status transitions from PENDING → SENT or PENDING → FAILED inside
 * NotificationServiceImpl with a try-catch around the email sender call.
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_id", columnList = "recipient_id"),
                @Index(name = "idx_notifications_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // FK → users.id — the customer receiving this notification
    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.EMAIL;

    // Email subject line
    @Column(nullable = false)
    private String subject;

    // Full email body (HTML or plain text)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    // Populated when status = SENT
    @Column
    private Instant sentAt;

    // Populated when status = FAILED — stores the exception message
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // JSON metadata: {"bookingId": "uuid", "paymentId": "uuid"}
    // Allows traceability back to the booking/payment that triggered this notification
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}