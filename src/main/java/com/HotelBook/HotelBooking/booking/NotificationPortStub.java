package com.HotelBook.HotelBooking.booking;



import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("!prod")
@Slf4j
public class NotificationPortStub implements NotificationPort {

    @Override
    public void sendBookingConfirmation(UUID customerId, UUID bookingId, String hotelName) {
        log.info("[STUB] Booking confirmation sent to customer {} for booking {} at {}",
                customerId, bookingId, hotelName);
    }

    @Override
    public void sendBookingCancellation(UUID customerId, UUID bookingId, BigDecimal refundAmount) {
        log.info("[STUB] Booking cancellation sent to customer {} for booking {} — refund: {}",
                customerId, bookingId, refundAmount);
    }

    @Override
    public void sendPaymentFailed(UUID customerId, UUID bookingId) {
        log.info("[STUB] Payment failure notification sent to customer {} for booking {}",
                customerId, bookingId);
    }
}
