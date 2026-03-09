package com.HotelBook.HotelBooking.payment;



import com.hotelapp.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/bookings/{bookingId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> processPayment(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId,
            @Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponseDTO payment = paymentService.processPayment(bookingId, customerId, request);

        String message = payment.getStatus().name().equals("PAID")
                ? "Payment successful. Booking is now confirmed and your dates are reserved."
                : "Payment failed. Your booking is still pending — please try again.";

        return ResponseEntity.ok(ApiResponse.success(message, payment));
    }


    @PostMapping("/api/bookings/{bookingId}/payment/refund")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> processRefund(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId) {

        PaymentResponseDTO payment = paymentService.processRefund(bookingId, customerId);

        String refundMsg = payment.getRefundAmount() != null && payment.getRefundAmount().compareTo(java.math.BigDecimal.ZERO) > 0
                ? "Booking cancelled. Refund of $" + payment.getRefundAmount() + " will be processed within 3–5 business days."
                : "Booking cancelled. No refund applies based on your selected cancellation policy.";

        return ResponseEntity.ok(ApiResponse.success(refundMsg, payment));
    }


    @GetMapping("/api/bookings/{bookingId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPaymentForBooking(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId) {

        PaymentResponseDTO payment = paymentService.getPaymentByBookingId(bookingId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved", payment));
    }


    @GetMapping("/api/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPaymentById(
            @PathVariable UUID paymentId) {

        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved", payment));
    }
}
