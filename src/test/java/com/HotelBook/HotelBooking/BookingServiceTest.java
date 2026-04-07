package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Booking.*;
import com.HotelBook.HotelBooking.Booking.NotificationPort;
import com.HotelBook.HotelBooking.Cancellation.CancellationPolicy;
import com.HotelBook.HotelBooking.Cancellation.CancellationPolicyResponseDTO;
import com.HotelBook.HotelBooking.Cancellation.CancellationPolicyService;
import com.HotelBook.HotelBooking.Common.exception.BadRequestException;
import com.HotelBook.HotelBooking.Common.exception.ConflictException;
import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.Pricing.PricingRuleService;
import com.HotelBook.HotelBooking.Room.Room;
import com.HotelBook.HotelBooking.Room.RoomRepository;
import com.HotelBook.HotelBooking.RoomAvailability.RoomAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    // ── Mocks ────────────────────────────────────────────────────────────────
    @Mock private BookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomAvailabilityService availabilityService;
    @Mock private PricingRuleService pricingService;
    @Mock private CancellationPolicyService cancellationPolicyService;
    @Mock private NotificationPort notificationPort;

    @InjectMocks
    private BookingService bookingService;

    // ── Shared fixtures ──────────────────────────────────────────────────────
    private UUID customerId;
    private UUID hotelId;
    private UUID roomId;
    private UUID bookingId;
    private Room activeRoom;
    private BookingRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        hotelId    = UUID.randomUUID();
        roomId     = UUID.randomUUID();
        bookingId  = UUID.randomUUID();

        activeRoom = Room.builder()
                .id(roomId)
                .hotelId(hotelId)
                .isActive(true)
                .maxAdults(2)
                .maxChildren(1)
                .quantity(5)
                .price(new BigDecimal("100.00"))
                .build();

        validRequest = new BookingRequestDTO();
        validRequest.setHotelId(hotelId);
        validRequest.setRoomId(roomId);
        validRequest.setCheckInDate(LocalDate.now().plusDays(1));
        validRequest.setCheckOutDate(LocalDate.now().plusDays(3));
        validRequest.setAdults(1);
        validRequest.setChildren(0);
        validRequest.setRoomCount(1);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  createBooking
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createBooking()")
    class CreateBooking {

        @Test
        @DisplayName("should create booking successfully when all inputs are valid")
        void shouldCreateBookingSuccessfully() {
            // Arrange
            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));
            given(availabilityService.isAvailable(eq(roomId), any(), any(), eq(1), eq(5)))
                    .willReturn(true);
            given(pricingService.calculateTotalPrice(eq(roomId), any(), any(), any()))
                    .willReturn(new BigDecimal("200.00"));
            given(cancellationPolicyService.getPoliciesForRoom(hotelId, roomId))
                    .willReturn(List.of());

            Booking savedBooking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.save(any(Booking.class))).willReturn(savedBooking);

            // Act
            BookingResponseDTO result = bookingService.createBooking(customerId, validRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when checkout is not after checkin")
        void shouldThrowWhenCheckoutNotAfterCheckin() {
            validRequest.setCheckInDate(LocalDate.now().plusDays(3));
            validRequest.setCheckOutDate(LocalDate.now().plusDays(1));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, validRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Check-out date must be after check-in date");
        }

        @Test
        @DisplayName("should throw BadRequestException when checkout equals checkin")
        void shouldThrowWhenCheckoutEqualsCheckin() {
            LocalDate same = LocalDate.now().plusDays(2);
            validRequest.setCheckInDate(same);
            validRequest.setCheckOutDate(same);

            assertThatThrownBy(() -> bookingService.createBooking(customerId, validRequest))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when room does not exist")
        void shouldThrowWhenRoomNotFound() {
            given(roomRepository.findById(roomId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBooking(customerId, validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Room");
        }

        @Test
        @DisplayName("should throw ConflictException when room is inactive")
        void shouldThrowWhenRoomIsInactive() {
            activeRoom.setIsActive(false);
            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, validRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("not currently available");
        }

        @Test
        @DisplayName("should throw BadRequestException when room belongs to a different hotel")
        void shouldThrowWhenRoomBelongsToDifferentHotel() {
            activeRoom.setHotelId(UUID.randomUUID()); // different hotel
            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, validRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not belong to hotel");
        }

        @Test
        @DisplayName("should throw BadRequestException when guest count exceeds room capacity")
        void shouldThrowWhenCapacityExceeded() {
            validRequest.setAdults(10);
            validRequest.setChildren(5);
            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));

            assertThatThrownBy(() -> bookingService.createBooking(customerId, validRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Room capacity exceeded");
        }

        @Test
        @DisplayName("should throw ConflictException when room is not available for selected dates")
        void shouldThrowWhenRoomNotAvailableForDates() {
            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));
            given(availabilityService.isAvailable(any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(false);

            assertThatThrownBy(() -> bookingService.createBooking(customerId, validRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("not available for the selected dates");
        }

        @Test
        @DisplayName("should apply cancellation policy price multiplier when policy is specified")
        void shouldApplyCancellationPolicyMultiplier() {
            UUID policyId = UUID.randomUUID();
            validRequest.setCancellationPolicyId(policyId);

            CancellationPolicy policy = CancellationPolicy.builder()
                    .id(policyId)
                    .priceMultiplier(new BigDecimal("1.20"))
                    .build();

            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));
            given(availabilityService.isAvailable(any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(true);
            given(pricingService.calculateTotalPrice(any(), any(), any(), any()))
                    .willReturn(new BigDecimal("200.00"));
            given(cancellationPolicyService.getPolicyEntityById(policyId)).willReturn(policy);

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            given(bookingRepository.save(captor.capture())).willAnswer(inv -> captor.getValue());

            bookingService.createBooking(customerId, validRequest);

            Booking saved = captor.getValue();
            // 200.00 base * 1 room * 1.20 multiplier = 240.00
            assertThat(saved.getTotalPrice()).isEqualByComparingTo("240.00");
        }

        @Test
        @DisplayName("should default children to 0 when not provided")
        void shouldDefaultChildrenToZeroWhenNull() {
            validRequest.setChildren(null);

            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));
            given(availabilityService.isAvailable(any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(true);
            given(pricingService.calculateTotalPrice(any(), any(), any(), any()))
                    .willReturn(new BigDecimal("200.00"));
            given(cancellationPolicyService.getPoliciesForRoom(hotelId, roomId))
                    .willReturn(List.of());

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            given(bookingRepository.save(captor.capture())).willAnswer(inv -> captor.getValue());

            bookingService.createBooking(customerId, validRequest);

            assertThat(captor.getValue().getChildren()).isEqualTo(0);
        }

        @Test
        @DisplayName("should default roomCount to 1 when not provided")
        void shouldDefaultRoomCountToOneWhenNull() {
            validRequest.setRoomCount(null);

            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));
            given(availabilityService.isAvailable(any(), any(), any(), eq(1), anyInt()))
                    .willReturn(true);
            given(pricingService.calculateTotalPrice(any(), any(), any(), any()))
                    .willReturn(new BigDecimal("200.00"));
            given(cancellationPolicyService.getPoliciesForRoom(hotelId, roomId))
                    .willReturn(List.of());

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            given(bookingRepository.save(captor.capture())).willAnswer(inv -> captor.getValue());

            bookingService.createBooking(customerId, validRequest);

            assertThat(captor.getValue().getRoomCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should use default policy when no policy specified but one exists")
        void shouldUseDefaultPolicyWhenNoneSpecified() {
            UUID defaultPolicyId = UUID.randomUUID();
            CancellationPolicyResponseDTO defaultPolicy = new CancellationPolicyResponseDTO();
            defaultPolicy.setId(defaultPolicyId);
            defaultPolicy.setIsDefault(true);
            defaultPolicy.setPriceMultiplier(BigDecimal.ONE);

            given(roomRepository.findById(roomId)).willReturn(Optional.of(activeRoom));
            given(availabilityService.isAvailable(any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(true);
            given(pricingService.calculateTotalPrice(any(), any(), any(), any()))
                    .willReturn(new BigDecimal("200.00"));
            given(cancellationPolicyService.getPoliciesForRoom(hotelId, roomId))
                    .willReturn(List.of(defaultPolicy));

            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
            given(bookingRepository.save(captor.capture())).willAnswer(inv -> captor.getValue());

            bookingService.createBooking(customerId, validRequest);

            assertThat(captor.getValue().getCancellationPolicyId()).isEqualTo(defaultPolicyId);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  confirmBooking
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("confirmBooking()")
    class ConfirmBooking {

        @Test
        @DisplayName("should confirm a PENDING booking successfully")
        void shouldConfirmPendingBooking() {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            bookingService.confirmBooking(bookingId);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            verify(availabilityService).blockDates(
                    eq(booking.getRoomId()), any(), any(), anyInt(), eq(bookingId));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when booking not found")
        void shouldThrowWhenBookingNotFound() {
            given(bookingRepository.findById(bookingId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.confirmBooking(bookingId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ConflictException when booking is already CONFIRMED")
        void shouldThrowWhenAlreadyConfirmed() {
            Booking booking = buildBooking(BookingStatus.CONFIRMED);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.confirmBooking(bookingId))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Cannot confirm booking");
        }

        @Test
        @DisplayName("should throw ConflictException when booking is CANCELLED")
        void shouldThrowWhenBookingCancelled() {
            Booking booking = buildBooking(BookingStatus.CANCELLED);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.confirmBooking(bookingId))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("should send notification after confirming booking")
        void shouldSendNotificationAfterConfirm() throws Exception {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            bookingService.confirmBooking(bookingId);

            verify(notificationPort).sendBookingConfirmation(eq(customerId), eq(bookingId), anyString());
        }

        @Test
        @DisplayName("should not propagate notification failure")
        void shouldNotPropagateNotificationFailure() throws Exception {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);
            doThrow(new RuntimeException("SMTP error"))
                    .when(notificationPort).sendBookingConfirmation(any(), any(), any());

            // Should not throw
            bookingService.confirmBooking(bookingId);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  cancelBooking
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("cancelBooking()")
    class CancelBooking {

        @Test
        @DisplayName("should cancel a PENDING booking")
        void shouldCancelPendingBooking() {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            BookingResponseDTO result = bookingService.cancelBooking(bookingId, customerId, "CUSTOMER");

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(booking.getCancelledBy()).isEqualTo("CUSTOMER");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should cancel a CONFIRMED booking and unblock dates")
        void shouldCancelConfirmedBookingAndUnblockDates() {
            Booking booking = buildBooking(BookingStatus.CONFIRMED);
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            bookingService.cancelBooking(bookingId, customerId, "CUSTOMER");

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            verify(availabilityService).unblockDates(
                    eq(roomId), any(), any(), anyInt());
        }

        @Test
        @DisplayName("should NOT unblock dates when cancelling a PENDING booking")
        void shouldNotUnblockDatesForPendingCancellation() {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            bookingService.cancelBooking(bookingId, customerId, "CUSTOMER");

            verify(availabilityService, never()).unblockDates(any(), any(), any(), anyInt());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when booking not found for customer")
        void shouldThrowWhenBookingNotFoundForCustomer() {
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, customerId, "CUSTOMER"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ConflictException when booking is COMPLETED")
        void shouldThrowWhenBookingCompleted() {
            Booking booking = buildBooking(BookingStatus.COMPLETED);
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, customerId, "CUSTOMER"))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Cannot cancel booking");
        }

        @Test
        @DisplayName("should throw ConflictException when booking is already CANCELLED")
        void shouldThrowWhenAlreadyCancelled() {
            Booking booking = buildBooking(BookingStatus.CANCELLED);
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, customerId, "CUSTOMER"))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("should set cancelledAt timestamp when cancelling")
        void shouldSetCancelledAtTimestamp() {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            bookingService.cancelBooking(bookingId, customerId, "CUSTOMER");

            assertThat(booking.getCancelledAt()).isNotNull();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  markFailed
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("markFailed()")
    class MarkFailed {

        @Test
        @DisplayName("should mark booking as FAILED")
        void shouldMarkBookingAsFailed() {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            bookingService.markFailed(bookingId);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.FAILED);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when booking not found")
        void shouldThrowWhenBookingNotFound() {
            given(bookingRepository.findById(bookingId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.markFailed(bookingId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should send payment failure notification")
        void shouldSendPaymentFailureNotification() throws Exception {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            bookingService.markFailed(bookingId);

            verify(notificationPort).sendPaymentFailed(eq(customerId), eq(bookingId));
        }

        @Test
        @DisplayName("should not propagate notification exception")
        void shouldNotPropagateNotificationException() throws Exception {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);
            doThrow(new RuntimeException("notification failed"))
                    .when(notificationPort).sendPaymentFailed(any(), any());

            // Should not throw
            bookingService.markFailed(bookingId);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  completeBooking
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("completeBooking()")
    class CompleteBooking {

        @Test
        @DisplayName("should complete a CONFIRMED booking")
        void shouldCompleteConfirmedBooking() {
            Booking booking = buildBooking(BookingStatus.CONFIRMED);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            BookingResponseDTO result = bookingService.completeBooking(bookingId);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should throw ConflictException when booking is PENDING")
        void shouldThrowWhenBookingPending() {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.completeBooking(bookingId))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Can only complete a CONFIRMED booking");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when booking not found")
        void shouldThrowWhenNotFound() {
            given(bookingRepository.findById(bookingId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.completeBooking(bookingId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  markNoShow
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("markNoShow()")
    class MarkNoShow {

        @Test
        @DisplayName("should mark a CONFIRMED booking as NO_SHOW")
        void shouldMarkConfirmedBookingAsNoShow() {
            Booking booking = buildBooking(BookingStatus.CONFIRMED);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.save(any())).willReturn(booking);

            BookingResponseDTO result = bookingService.markNoShow(bookingId);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.NO_SHOW);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should throw ConflictException when booking is not CONFIRMED")
        void shouldThrowWhenNotConfirmed() {
            Booking booking = buildBooking(BookingStatus.PENDING);
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.markNoShow(bookingId))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Can only mark NO_SHOW for a CONFIRMED booking");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getBookingById
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getBookingById()")
    class GetBookingById {

        @Test
        @DisplayName("should return booking when found for the given customer")
        void shouldReturnBookingForCustomer() {
            Booking booking = buildBooking(BookingStatus.CONFIRMED);
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.of(booking));

            BookingResponseDTO result = bookingService.getBookingById(bookingId, customerId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(bookingId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when booking not found for customer")
        void shouldThrowWhenNotFoundForCustomer() {
            given(bookingRepository.findByIdAndCustomerId(bookingId, customerId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getBookingById(bookingId, customerId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getBookingsByCustomer
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getBookingsByCustomer()")
    class GetBookingsByCustomer {

        @Test
        @DisplayName("should return all bookings for a customer ordered by createdAt desc")
        void shouldReturnBookingsForCustomer() {
            Booking b1 = buildBooking(BookingStatus.CONFIRMED);
            Booking b2 = buildBooking(BookingStatus.CANCELLED);
            given(bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId))
                    .willReturn(List.of(b1, b2));

            List<BookingResponseDTO> result = bookingService.getBookingsByCustomer(customerId);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when customer has no bookings")
        void shouldReturnEmptyListWhenNoBookings() {
            given(bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId))
                    .willReturn(List.of());

            List<BookingResponseDTO> result = bookingService.getBookingsByCustomer(customerId);

            assertThat(result).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getBookingsByHotel
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getBookingsByHotel()")
    class GetBookingsByHotel {

        @Test
        @DisplayName("should return all bookings for a hotel ordered by checkInDate asc")
        void shouldReturnBookingsForHotel() {
            Booking b1 = buildBooking(BookingStatus.CONFIRMED);
            given(bookingRepository.findByHotelIdOrderByCheckInDateAsc(hotelId))
                    .willReturn(List.of(b1));

            List<BookingResponseDTO> result = bookingService.getBookingsByHotel(hotelId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when hotel has no bookings")
        void shouldReturnEmptyListForHotelWithNoBookings() {
            given(bookingRepository.findByHotelIdOrderByCheckInDateAsc(hotelId))
                    .willReturn(List.of());

            List<BookingResponseDTO> result = bookingService.getBookingsByHotel(hotelId);

            assertThat(result).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Helper
    // ═════════════════════════════════════════════════════════════════════════
    private Booking buildBooking(BookingStatus status) {
        return Booking.builder()
                .id(bookingId)
                .customerId(customerId)
                .hotelId(hotelId)
                .roomId(roomId)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .adults(1)
                .children(0)
                .roomCount(1)
                .pricePerNight(new BigDecimal("100.00"))
                .totalPrice(new BigDecimal("200.00"))
                .status(status)
                .build();
    }
}
