package com.HotelBook.HotelBooking.booking;

public interface NotificationPort {


    void sendBookingConfirmation(java.util.UUID customerId,
                                 java.util.UUID bookingId,
                                 String hotelName);


    void sendBookingCancellation(java.util.UUID customerId,
                                 java.util.UUID bookingId,
                                 java.math.BigDecimal refundAmount);


    void sendPaymentFailed(java.util.UUID customerId,
                           java.util.UUID bookingId);
}
