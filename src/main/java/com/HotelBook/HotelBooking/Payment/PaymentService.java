package com.HotelBook.HotelBooking.Payment;

import com.HotelBook.HotelBooking.Booking.Booking;
import com.HotelBook.HotelBooking.Booking.BookingRepository;
import com.HotelBook.HotelBooking.Booking.BookingService;
import com.HotelBook.HotelBooking.Booking.BookingStatus;
import com.HotelBook.HotelBooking.Cancellation.CancellationPolicy;
import com.HotelBook.HotelBooking.Cancellation.CancellationPolicyService;
import com.HotelBook.HotelBooking.Common.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final CancellationPolicyService cancellationPolicyService;


    @Transactional
    public PaymentResponseDTO processPayment(UUID bookingId, UUID customerId,
                                             PaymentRequestDTO request) {

        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException(
                    "Cannot process payment for booking with status: " + booking.getStatus() +
                            ". Only PENDING bookings can be paid.");
        }

       if (paymentRepository.existsByBookingId(bookingId)) {
            throw new ConflictException(
                    "A payment record already exists for booking: " + bookingId +
                            ". Check payment status before retrying.");
        }

        boolean success = !Boolean.TRUE.equals(request.getSimulateFailure());

        if (success) {
            return processSuccessfulPayment(booking, request.getPaymentMethod());
        } else {
            return processFailedPayment(booking, request.getPaymentMethod());
        }
    }


    private PaymentResponseDTO processSuccessfulPayment(Booking booking, String paymentMethod) {
        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .customerId(booking.getCustomerId())
                .amount(booking.getTotalPrice())
                .status(PaymentStatus.PAID)
                .paymentMethod(paymentMethod)
                .paidAt(LocalDateTime.now())
                .notes("Payment processed successfully via " + paymentMethod)
                .build();

        Payment saved = paymentRepository.save(payment);

       bookingService.confirmBooking(booking.getId());

        log.info("Payment {} PAID for booking {} — amount: {}",
                saved.getId(), booking.getId(), booking.getTotalPrice());

        return toResponseDTO(saved);
    }


    private PaymentResponseDTO processFailedPayment(Booking booking, String paymentMethod) {
        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .customerId(booking.getCustomerId())
                .amount(booking.getTotalPrice())
                .status(PaymentStatus.FAILED)
                .paymentMethod(paymentMethod)
                .paidAt(null)
                .notes("Payment processing failed (simulated failure)")
                .build();

        Payment saved = paymentRepository.save(payment);

        bookingService.markFailed(booking.getId());

        log.info("Payment FAILED for booking {} — method: {}", booking.getId(), paymentMethod);

        return toResponseDTO(saved);
    }


    @Transactional
    public PaymentResponseDTO processRefund(UUID bookingId, UUID customerId) {

       Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!booking.isCancellable()) {
            throw new ConflictException(
                    "Cannot refund booking with status: " + booking.getStatus() +
                            ". Only PENDING or CONFIRMED bookings can be refunded.");
        }

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BadRequestException(
                        "No payment found for booking: " + bookingId +
                                ". Cannot issue refund for an unpaid booking."));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new ConflictException(
                    "Cannot refund payment with status: " + payment.getStatus() +
                            ". Only PAID payments can be refunded.");
        }

       BigDecimal refundAmount = BigDecimal.ZERO;
        String refundNote;

        if (booking.getCancellationPolicyId() != null) {

            LocalDateTime checkInDateTime = booking.getCheckInDate().atTime(14, 0); // 2pm check-in
            LocalDateTime cancelledAt = LocalDateTime.now();

            refundAmount = cancellationPolicyService.calculateRefund(
                    booking.getCancellationPolicyId(),
                    cancelledAt,
                    checkInDateTime,
                    payment.getAmount()
            );

            CancellationPolicy policy = cancellationPolicyService
                    .getPolicyEntityById(booking.getCancellationPolicyId());

            if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
                refundNote = "No refund — " + policy.getTierName() +
                        " (cancelled after " + policy.getDeadlineHours() + "h deadline or non-refundable)";
            } else if (refundAmount.compareTo(payment.getAmount()) == 0) {
                refundNote = "Full refund — " + policy.getTierName() +
                        " (cancelled within " + policy.getDeadlineHours() + "h deadline)";
            } else {
                refundNote = policy.getRefundPercentage() + "% partial refund — " + policy.getTierName();
            }
        } else {
            refundAmount = payment.getAmount();
            refundNote = "Full refund — no cancellation policy configured";
        }

       payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(refundAmount);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setNotes(refundNote);
        Payment saved = paymentRepository.save(payment);

       bookingService.cancelBooking(bookingId, customerId, "CUSTOMER");

        log.info("Refund {} processed for booking {} — refundAmount: {} (paid: {})",
                saved.getId(), bookingId, refundAmount, payment.getAmount());

        return toResponseDTO(saved);
    }


    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByBookingId(UUID bookingId, UUID customerId) {
        Payment payment = paymentRepository.findByBookingIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for booking: " + bookingId));
        return toResponseDTO(payment);
    }


    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        return toResponseDTO(payment);
    }


    private PaymentResponseDTO toResponseDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setBookingId(payment.getBookingId());
        dto.setCustomerId(payment.getCustomerId());
        dto.setAmount(payment.getAmount());
        dto.setRefundAmount(payment.getRefundAmount());
        dto.setStatus(payment.getStatus());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaidAt(payment.getPaidAt());
        dto.setRefundedAt(payment.getRefundedAt());
        dto.setNotes(payment.getNotes());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}