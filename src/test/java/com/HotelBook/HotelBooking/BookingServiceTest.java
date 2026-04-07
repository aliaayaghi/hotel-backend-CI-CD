package com.HotelBook.HotelBooking;




import com.HotelBook.HotelBooking.Booking.*;
import com.HotelBook.HotelBooking.Booking.NotificationPort;
import com.HotelBook.HotelBooking.Cancellation.CancellationPolicyService;
import com.HotelBook.HotelBooking.Common.exception.BadRequestException;
import com.HotelBook.HotelBooking.Common.exception.ConflictException;
import com.HotelBook.HotelBooking.Pricing.PricingRuleService;
import com.HotelBook.HotelBooking.Room.Room;
import com.HotelBook.HotelBooking.Room.RoomRepository;
import com.HotelBook.HotelBooking.RoomAvailability.RoomAvailabilityService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomAvailabilityService availabilityService;
    @Mock private PricingRuleService pricingService;
    @Mock private CancellationPolicyService cancellationPolicyService;
    @Mock private NotificationPort notificationPort;

    @InjectMocks
    private BookingService bookingService;

    private UUID customerId;
    private BookingRequestDTO request;
    private Room room;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();

        request = new BookingRequestDTO();
        request.setHotelId(hotelId);
        request.setRoomId(roomId);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));
        request.setAdults(2);
        request.setRoomCount(1);

        room = new Room();
        room.setId(roomId);
        room.setHotelId(hotelId);
        room.setIsActive(true);
        room.setMaxAdults(2);
        room.setMaxChildren(2);
        room.setPrice(new BigDecimal("100.00"));
        room.setQuantity(10);
    }

    @Test
    @DisplayName("Should throw BadRequestException when checkout is before checkin")
    void createBooking_InvalidDates_ThrowsException() {
        request.setCheckOutDate(request.getCheckInDate().minusDays(1));

        assertThrows(BadRequestException.class, () ->
                bookingService.createBooking(customerId, request)
        );
    }

    @Test
    @DisplayName("Should create booking successfully on happy path")
    void createBooking_Success() {
        // Arrange
        when(roomRepository.findById(request.getRoomId())).thenReturn(Optional.of(room));
        when(availabilityService.isAvailable(any(), any(), any(), anyInt(), anyInt())).thenReturn(true);
        when(pricingService.calculateTotalPrice(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("200.00"));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        // Act
        BookingResponseDTO response = bookingService.createBooking(customerId, request);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("200.00"), response.getTotalPrice());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should confirm booking and block dates")
    void confirmBooking_Success() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = Booking.builder()
                .id(bookingId)
                .status(BookingStatus.PENDING)
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(1))
                .roomCount(1)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.confirmBooking(bookingId);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(availabilityService).blockDates(any(), any(), any(), anyInt(), any());
        verify(notificationPort).sendBookingConfirmation(any(), any(), anyString());
    }
}