package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Booking.Booking;
import com.HotelBook.HotelBooking.Booking.BookingRepository;
import com.HotelBook.HotelBooking.Common.exception.ReviewAlreadyExistsException;
import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.Review.repository.ReviewRepository;
import com.HotelBook.HotelBooking.Review.service.ReviewServiceImpl;
import com.HotelBook.HotelBooking.User.entity.Customer;
import com.HotelBook.HotelBooking.User.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private HotelRepository hotelRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID bookingId;
    private ReviewRequestDTO request;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();
        request = new ReviewRequestDTO();
        request.setBookingId(bookingId);
        request.setHotelId(UUID.randomUUID());
        request.setCustomerId(UUID.randomUUID());
    }

    @Test
    @DisplayName("createReview: Should throw exception if booking already has a review")
    void createReview_DuplicateBooking_ThrowsException() {
        // Arrange
        when(reviewRepository.existsByBookingId(bookingId)).thenReturn(true);

        // Act & Assert
        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.createReview(request));
        verify(reviewRepository, never()).save(any());
    }



    @Test
    @DisplayName("validateSort: Should throw exception for unauthorized sort fields")
    void listReviews_InvalidSortField_ThrowsException() {
        // Arrange: "password" is not in ALLOWED_SORT_FIELDS
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("password"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                reviewService.listReviews(pageable, null, null, null, null, null, null, false)
        );
    }
}
