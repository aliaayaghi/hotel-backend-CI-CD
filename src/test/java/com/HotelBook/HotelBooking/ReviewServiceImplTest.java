package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Booking.Booking;
import com.HotelBook.HotelBooking.Booking.BookingRepository;
import com.HotelBook.HotelBooking.Common.exception.ReviewAlreadyExistsException;
import com.HotelBook.HotelBooking.Common.exception.ReviewNotFoundException;
import com.HotelBook.HotelBooking.Common.pagination.PagedResponse;
import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.Review.repository.ReviewRepository;
import com.HotelBook.HotelBooking.Review.service.ReviewServiceImpl;
import com.HotelBook.HotelBooking.User.entity.Customer;
import com.HotelBook.HotelBooking.User.entity.User;
import com.HotelBook.HotelBooking.User.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReviewServiceImpl}.
 *
 * Strategy: pure unit tests with Mockito — no Spring context, no DB.
 * Every collaborator (ReviewRepository, HotelRepository, CustomerRepository,
 * BookingRepository) is mocked so tests run in milliseconds.
 *
 * Test layout:
 *  - CreateReview      → happy path, duplicate booking, hotel/customer/booking not found
 *  - GetReviewById     → found, not found
 *  - ListReviews       → no filters, with filters, invalid sort field
 *  - GetByHotelId      → delegates correctly
 *  - GetByCustomerId   → delegates correctly
 *  - AddManagerReply   → sets reply + repliedAt, not found
 *  - FlagReview        → sets flagged=true, not found
 *  - HideReview        → sets hidden=true, not found
 *  - DeleteReview      → exists → deleted, not found
 *  - GetAverageScores  → assembles map from 6 repo queries
 *  - GetAverageRating  → delegates to repo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewServiceImpl — Unit Tests")
class ReviewServiceImplTest {

    // ── Mocks ─────────────────────────────────────────────────────────────────

    @Mock private ReviewRepository   reviewRepository;
    @Mock private HotelRepository    hotelRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private BookingRepository  bookingRepository;

    @InjectMocks
    private ReviewServiceImpl service;

    // ── Shared test fixtures ──────────────────────────────────────────────────

    private UUID reviewId;
    private UUID hotelId;
    private UUID customerId;
    private UUID bookingId;

    private Hotel    hotel;
    private Customer customer;
    private Booking  booking;
    private Review   review;
    private ReviewRequestDTO request;

    // ── Helper: build a minimal User stub for Customer.getUser().getName() ───

    private static User stubUser(String name) {
        User u = new User();
        u.setName(name);
        return u;
    }

    @BeforeEach
    void setUp() {
        reviewId   = UUID.randomUUID();
        hotelId    = UUID.randomUUID();
        customerId = UUID.randomUUID();
        bookingId  = UUID.randomUUID();

        // --- Hotel stub ---
        hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Grand Azure Hotel");

        // --- Customer stub ---
        customer = new Customer();
        customer.setId(customerId);
        customer.setUser(stubUser("Ahmed Mansour"));

        // --- Booking stub ---
        booking = new Booking();
        booking.setId(bookingId);

        // --- Review entity ---
        review = new Review();
        review.setId(reviewId);
        review.setHotel(hotel);
        review.setCustomer(customer);
        review.setBooking(booking);
        review.setCleanlinessScore(8);
        review.setLocationScore(9);
        review.setValueScore(7);
        review.setComfortScore(8);
        review.setServiceScore(9);
        review.setCustomerOverallRating(8);
        review.setCalculatedOverallRating(8.2);
        review.setTitle("Great stay!");
        review.setComment("Loved the infinity pool.");
        review.setTravelType(Review.TravelType.COUPLE);
        review.setFlagged(false);
        review.setHidden(false);
        review.setCreatedAt(LocalDateTime.now());

        // --- Request DTO ---
        request = new ReviewRequestDTO();
        request.setHotelId(hotelId);
        request.setCustomerId(customerId);
        request.setBookingId(bookingId);
        request.setCleanlinessScore(8);
        request.setLocationScore(9);
        request.setValueScore(7);
        request.setComfortScore(8);
        request.setServiceScore(9);
        request.setCustomerOverallRating(8);
        request.setTitle("Great stay!");
        request.setComment("Loved the infinity pool.");
        request.setTravelType(Review.TravelType.COUPLE);
    }

    // =========================================================================
    // createReview
    // =========================================================================
    @Nested
    @DisplayName("createReview()")
    class CreateReview {

        @Test
        @DisplayName("Happy path — saves review and returns DTO")
        void createReview_happyPath_returnsDTO() {
            // Given
            when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);
            when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(reviewRepository.save(any(Review.class))).thenReturn(review);

            // When
            ReviewResponseDTO result = service.createReview(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getHotelId()).isEqualTo(hotelId);
            assertThat(result.getCustomerId()).isEqualTo(customerId);
            assertThat(result.getBookingId()).isEqualTo(bookingId);
            assertThat(result.getTitle()).isEqualTo("Great stay!");
            assertThat(result.getTravelType()).isEqualTo(Review.TravelType.COUPLE);

            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("Duplicate booking → throws ReviewAlreadyExistsException")
        void createReview_duplicateBooking_throwsException() {
            // Given
            when(reviewRepository.existsByBookingId(bookingId)).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> service.createReview(request))
                    .isInstanceOf(ReviewAlreadyExistsException.class)
                    .hasMessageContaining(bookingId.toString());

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Hotel not found → throws RuntimeException")
        void createReview_hotelNotFound_throwsException() {
            when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);
            when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createReview(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Hotel not found");

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Customer not found → throws RuntimeException")
        void createReview_customerNotFound_throwsException() {
            when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);
            when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
            when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createReview(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Customer not found");

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Booking not found → throws RuntimeException")
        void createReview_bookingNotFound_throwsException() {
            when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);
            when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createReview(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Booking not found");

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("CalculatedOverallRating is average of 5 sub-scores")
        void createReview_calculatedRatingIsCorrectAverage() {
            // scores: 8+9+7+8+9 = 41 / 5 = 8.2
            when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);
            when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
            when(reviewRepository.save(captor.capture())).thenReturn(review);

            service.createReview(request);

            Review saved = captor.getValue();
            assertThat(saved.getCalculatedOverallRating()).isEqualTo(8.2);
        }
    }

    // =========================================================================
    // getReviewById
    // =========================================================================
    @Nested
    @DisplayName("getReviewById()")
    class GetReviewById {

        @Test
        @DisplayName("Found → returns mapped DTO")
        void getReviewById_found_returnsDTO() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            ReviewResponseDTO result = service.getReviewById(reviewId);

            assertThat(result.getId()).isEqualTo(reviewId);
            assertThat(result.getHotelName()).isEqualTo("Grand Azure Hotel");
            assertThat(result.getCustomerName()).isEqualTo("Ahmed Mansour");
        }

        @Test
        @DisplayName("Not found → throws ReviewNotFoundException")
        void getReviewById_notFound_throwsException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getReviewById(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining(reviewId.toString());
        }
    }

    // =========================================================================
    // listReviews
    // =========================================================================
    @Nested
    @DisplayName("listReviews()")
    class ListReviews {

        private Pageable pageable;

        @BeforeEach
        void setUpPageable() {
            // Sort by a valid field so validateSort passes
            pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        }

        @Test
        @DisplayName("No filters → returns all non-hidden reviews as paged response")
        void listReviews_noFilters_returnsPagedResponse() {
            Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
            when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            PagedResponse<ReviewResponseDTO> result = service.listReviews(
                    pageable, null, null, null, null, null, null, null);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("With hotelId filter → spec passed to repository")
        void listReviews_withHotelIdFilter_passesSpecToRepo() {
            Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
            when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            PagedResponse<ReviewResponseDTO> result = service.listReviews(
                    pageable, hotelId, null, null, null, null, null, null);

            assertThat(result.getContent()).hasSize(1);
            verify(reviewRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("With minRating filter → spec passed to repository")
        void listReviews_withMinRatingFilter_passesSpecToRepo() {
            Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
            when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            PagedResponse<ReviewResponseDTO> result = service.listReviews(
                    pageable, null, null, null, 7, null, null, null);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("With onlyFlagged=true → spec passed to repository")
        void listReviews_withOnlyFlaggedTrue_passesSpecToRepo() {
            Page<Review> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            PagedResponse<ReviewResponseDTO> result = service.listReviews(
                    pageable, null, null, null, null, null, null, true);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Invalid sort field → throws RuntimeException")
        void listReviews_invalidSortField_throwsException() {
            Pageable badSort = PageRequest.of(0, 10, Sort.by("title")); // 'title' not in ALLOWED_SORT_FIELDS

            assertThatThrownBy(() ->
                    service.listReviews(badSort, null, null, null, null, null, null, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid sort field: title");
        }

        @Test
        @DisplayName("createdAfter + createdBefore filter → spec passed to repository")
        void listReviews_withDateRangeFilter_passesSpecToRepo() {
            Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
            when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            LocalDateTime after  = LocalDateTime.now().minusDays(7);
            LocalDateTime before = LocalDateTime.now();

            PagedResponse<ReviewResponseDTO> result = service.listReviews(
                    pageable, null, null, null, null, after, before, null);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("With travelType filter → spec passed to repository")
        void listReviews_withTravelTypeFilter_passesSpecToRepo() {
            Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
            when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            PagedResponse<ReviewResponseDTO> result = service.listReviews(
                    pageable, null, null, Review.TravelType.COUPLE, null, null, null, null);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    // =========================================================================
    // getReviewsByHotelId
    // =========================================================================
    @Nested
    @DisplayName("getReviewsByHotelId()")
    class GetReviewsByHotelId {

        @Test
        @DisplayName("Returns paged response of reviews for given hotel")
        void getReviewsByHotelId_returnsPagedResponse() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
            when(reviewRepository.findByHotelId(hotelId, pageable)).thenReturn(page);

            PagedResponse<ReviewResponseDTO> result = service.getReviewsByHotelId(hotelId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getHotelId()).isEqualTo(hotelId);
            verify(reviewRepository).findByHotelId(hotelId, pageable);
        }

        @Test
        @DisplayName("No reviews for hotel → returns empty paged response")
        void getReviewsByHotelId_noReviews_returnsEmpty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(reviewRepository.findByHotelId(hotelId, pageable)).thenReturn(emptyPage);

            PagedResponse<ReviewResponseDTO> result = service.getReviewsByHotelId(hotelId, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =========================================================================
    // getReviewsByCustomerId
    // =========================================================================
    @Nested
    @DisplayName("getReviewsByCustomerId()")
    class GetReviewsByCustomerId {

        @Test
        @DisplayName("Returns paged response of reviews for given customer")
        void getReviewsByCustomerId_returnsPagedResponse() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> page = new PageImpl<>(List.of(review), pageable, 1);
            when(reviewRepository.findByCustomerId(customerId, pageable)).thenReturn(page);

            PagedResponse<ReviewResponseDTO> result = service.getReviewsByCustomerId(customerId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCustomerId()).isEqualTo(customerId);
            verify(reviewRepository).findByCustomerId(customerId, pageable);
        }
    }

    // =========================================================================
    // addManagerReply
    // =========================================================================
    @Nested
    @DisplayName("addManagerReply()")
    class AddManagerReply {

        @Test
        @DisplayName("Found → sets reply text and repliedAt, returns updated DTO")
        void addManagerReply_found_setsReplyAndTimestamp() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.save(review)).thenReturn(review);

            ReviewResponseDTO result = service.addManagerReply(reviewId, "Thank you for your feedback!");

            assertThat(review.getManagerReply()).isEqualTo("Thank you for your feedback!");
            assertThat(review.getRepliedAt()).isNotNull();
            assertThat(review.getRepliedAt()).isBeforeOrEqualTo(LocalDateTime.now());
            verify(reviewRepository).save(review);
        }

        @Test
        @DisplayName("Not found → throws ReviewNotFoundException")
        void addManagerReply_notFound_throwsException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addManagerReply(reviewId, "Some reply"))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining(reviewId.toString());

            verify(reviewRepository, never()).save(any());
        }
    }

    // =========================================================================
    // flagReview
    // =========================================================================
    @Nested
    @DisplayName("flagReview()")
    class FlagReview {

        @Test
        @DisplayName("Found → sets flagged=true and returns updated DTO")
        void flagReview_found_setsFlaggedTrue() {
            review.setFlagged(false);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.save(review)).thenReturn(review);

            ReviewResponseDTO result = service.flagReview(reviewId);

            assertThat(review.isFlagged()).isTrue();
            assertThat(result.isFlagged()).isTrue();
            verify(reviewRepository).save(review);
        }

        @Test
        @DisplayName("Not found → throws ReviewNotFoundException")
        void flagReview_notFound_throwsException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.flagReview(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining(reviewId.toString());

            verify(reviewRepository, never()).save(any());
        }
    }

    // =========================================================================
    // hideReview
    // =========================================================================
    @Nested
    @DisplayName("hideReview()")
    class HideReview {

        @Test
        @DisplayName("Found → sets hidden=true and returns updated DTO")
        void hideReview_found_setsHiddenTrue() {
            review.setHidden(false);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.save(review)).thenReturn(review);

            ReviewResponseDTO result = service.hideReview(reviewId);

            assertThat(review.isHidden()).isTrue();
            assertThat(result.isHidden()).isTrue();
            verify(reviewRepository).save(review);
        }

        @Test
        @DisplayName("Not found → throws ReviewNotFoundException")
        void hideReview_notFound_throwsException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.hideReview(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining(reviewId.toString());

            verify(reviewRepository, never()).save(any());
        }
    }

    // =========================================================================
    // deleteReview
    // =========================================================================
    @Nested
    @DisplayName("deleteReview()")
    class DeleteReview {

        @Test
        @DisplayName("Found → calls deleteById and returns without error")
        void deleteReview_found_deletesSuccessfully() {
            when(reviewRepository.existsById(reviewId)).thenReturn(true);
            doNothing().when(reviewRepository).deleteById(reviewId);

            assertThatCode(() -> service.deleteReview(reviewId))
                    .doesNotThrowAnyException();

            verify(reviewRepository).deleteById(reviewId);
        }

        @Test
        @DisplayName("Not found → throws ReviewNotFoundException, deleteById not called")
        void deleteReview_notFound_throwsException() {
            when(reviewRepository.existsById(reviewId)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteReview(reviewId))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining(reviewId.toString());

            verify(reviewRepository, never()).deleteById(any());
        }
    }

    // =========================================================================
    // getAverageScoresForHotel
    // =========================================================================
    @Nested
    @DisplayName("getAverageScoresForHotel()")
    class GetAverageScoresForHotel {

        @Test
        @DisplayName("Returns map with all 6 score keys populated from repository")
        void getAverageScoresForHotel_returnsCompleteMap() {
            when(reviewRepository.getAverageCleanlinessScore(hotelId)).thenReturn(8.5);
            when(reviewRepository.getAverageLocationScore(hotelId)).thenReturn(9.0);
            when(reviewRepository.getAverageServiceScore(hotelId)).thenReturn(8.0);
            when(reviewRepository.getAverageValueScore(hotelId)).thenReturn(7.5);
            when(reviewRepository.getAverageComfortScore(hotelId)).thenReturn(8.2);
            when(reviewRepository.getAverageRatingForHotel(hotelId)).thenReturn(8.24);

            Map<String, Double> scores = service.getAverageScoresForHotel(hotelId);

            assertThat(scores).containsKeys("cleanliness", "location", "service", "value", "comfort", "overall");
            assertThat(scores.get("cleanliness")).isEqualTo(8.5);
            assertThat(scores.get("location")).isEqualTo(9.0);
            assertThat(scores.get("service")).isEqualTo(8.0);
            assertThat(scores.get("value")).isEqualTo(7.5);
            assertThat(scores.get("comfort")).isEqualTo(8.2);
            assertThat(scores.get("overall")).isEqualTo(8.24);
        }

        @Test
        @DisplayName("No reviews for hotel → all scores are null")
        void getAverageScoresForHotel_noReviews_allScoresNull() {
            when(reviewRepository.getAverageCleanlinessScore(hotelId)).thenReturn(null);
            when(reviewRepository.getAverageLocationScore(hotelId)).thenReturn(null);
            when(reviewRepository.getAverageServiceScore(hotelId)).thenReturn(null);
            when(reviewRepository.getAverageValueScore(hotelId)).thenReturn(null);
            when(reviewRepository.getAverageComfortScore(hotelId)).thenReturn(null);
            when(reviewRepository.getAverageRatingForHotel(hotelId)).thenReturn(null);

            Map<String, Double> scores = service.getAverageScoresForHotel(hotelId);

            assertThat(scores).containsKeys("cleanliness", "location", "service", "value", "comfort", "overall");
            assertThat(scores.values()).allSatisfy(v -> assertThat(v).isNull());
        }
    }

    // =========================================================================
    // getAverageRatingForHotel
    // =========================================================================
    @Nested
    @DisplayName("getAverageRatingForHotel()")
    class GetAverageRatingForHotel {

        @Test
        @DisplayName("Delegates to repository and returns result")
        void getAverageRatingForHotel_delegatesToRepo() {
            when(reviewRepository.getAverageRatingForHotel(hotelId)).thenReturn(8.7);

            Double result = service.getAverageRatingForHotel(hotelId);

            assertThat(result).isEqualTo(8.7);
            verify(reviewRepository).getAverageRatingForHotel(hotelId);
        }

        @Test
        @DisplayName("No reviews → returns null")
        void getAverageRatingForHotel_noReviews_returnsNull() {
            when(reviewRepository.getAverageRatingForHotel(hotelId)).thenReturn(null);

            Double result = service.getAverageRatingForHotel(hotelId);

            assertThat(result).isNull();
        }
    }
}

