package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Booking.Booking;
import com.HotelBook.HotelBooking.Booking.BookingRepository;
import com.HotelBook.HotelBooking.Booking.BookingService;
import com.HotelBook.HotelBooking.Booking.BookingStatus;
import com.HotelBook.HotelBooking.Cancellation.CancellationPolicyService;
import com.HotelBook.HotelBooking.Common.exception.ConflictException;
import com.HotelBook.HotelBooking.Payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private BookingService bookingService;
    @Mock private CancellationPolicyService cancellationPolicyService;

    @InjectMocks
    private PaymentService paymentService;

    private UUID bookingId;
    private UUID customerId;
    private Booking pendingBooking;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        pendingBooking = new Booking();
        pendingBooking.setId(bookingId);
        pendingBooking.setCustomerId(customerId);
        pendingBooking.setStatus(BookingStatus.PENDING);
        pendingBooking.setTotalPrice(new BigDecimal("200.00"));
        pendingBooking.setCheckInDate(LocalDate.now().plusDays(5));
    }

    @Test
    @DisplayName("processPayment: Should confirm booking on successful payment simulation")
    void processPayment_Success() {
        // Arrange
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPaymentMethod("CREDIT_CARD");
        request.setSimulateFailure(false);

        when(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                .thenReturn(Optional.of(pendingBooking));
        when(paymentRepository.existsByBookingId(bookingId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PaymentResponseDTO response = paymentService.processPayment(bookingId, customerId, request);

        // Assert
        assertEquals(PaymentStatus.PAID, response.getStatus());
        verify(bookingService).confirmBooking(bookingId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("processPayment: Should throw ConflictException if booking is already paid/confirmed")
    void processPayment_AlreadyConfirmed() {
        // Arrange
        pendingBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                .thenReturn(Optional.of(pendingBooking));

        // Act & Assert
        assertThrows(ConflictException.class, () ->
                paymentService.processPayment(bookingId, customerId, new PaymentRequestDTO())
        );
    }

    @Test
    @DisplayName("processRefund: Should calculate partial refund based on policy")
    void processRefund_Partial() {
        // Arrange
        UUID policyId = UUID.randomUUID();
        pendingBooking.setCancellationPolicyId(policyId);
        pendingBooking.setStatus(BookingStatus.CONFIRMED);

        Payment originalPayment = Payment.builder()
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PAID)
                .build();

        when(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                .thenReturn(Optional.of(pendingBooking));
        when(paymentRepository.findByBookingId(bookingId))
                .thenReturn(Optional.of(originalPayment));

        // Mock 50% refund logic
        when(cancellationPolicyService.calculateRefund(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("50.00"));
        when(cancellationPolicyService.getPolicyEntityById(policyId))
                .thenReturn(com.HotelBook.HotelBooking.Cancellation.CancellationPolicy.builder()
                        .tierName("Flexible").refundPercentage(50).build());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PaymentResponseDTO response = paymentService.processRefund(bookingId, customerId);

        // Assert
        assertEquals(PaymentStatus.REFUNDED, response.getStatus());
        assertEquals(new BigDecimal("50.00"), response.getRefundAmount());
        verify(bookingService).cancelBooking(eq(bookingId), eq(customerId), anyString());
    }
}
