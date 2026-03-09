package com.HotelBook.HotelBooking.booking;


import com.HotelBook.HotelBooking.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> createBooking(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @Valid @RequestBody BookingRequestDTO request) {

        BookingResponseDTO booking = bookingService.createBooking(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully. Proceed to payment.", booking));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getMyBookings(
            @RequestHeader("X-Customer-Id") UUID customerId) {

        List<BookingResponseDTO> bookings = bookingService.getBookingsByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved", bookings));
    }


    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> getBooking(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId) {

        BookingResponseDTO booking = bookingService.getBookingById(bookingId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved", booking));
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> cancelBooking(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId) {

        BookingResponseDTO booking = bookingService.cancelBooking(bookingId, customerId, "CUSTOMER");
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully.", booking));
    }

    @PatchMapping("/{bookingId}/complete")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> completeBooking(
            @PathVariable UUID bookingId) {

        BookingResponseDTO booking = bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking marked as completed.", booking));
    }


    @PatchMapping("/{bookingId}/no-show")
    public ResponseEntity<ApiResponse<BookingResponseDTO>> markNoShow(
            @PathVariable UUID bookingId) {

        BookingResponseDTO booking = bookingService.markNoShow(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking marked as no-show.", booking));
    }


    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<ApiResponse<List<BookingResponseDTO>>> getHotelBookings(
            @PathVariable UUID hotelId) {

        List<BookingResponseDTO> bookings = bookingService.getBookingsByHotel(hotelId);
        return ResponseEntity.ok(ApiResponse.success("Hotel bookings retrieved", bookings));
    }
}
