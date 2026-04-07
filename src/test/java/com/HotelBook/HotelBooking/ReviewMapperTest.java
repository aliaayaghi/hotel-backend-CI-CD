package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Booking.Booking;
import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.Review.mapper.ReviewMapper;
import com.HotelBook.HotelBooking.User.entity.Customer;
import com.HotelBook.HotelBooking.User.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ReviewMapper}.
 *
 * ReviewMapper is a pure static utility class — no Spring context needed.
 * Tests verify:
 *  - toEntity()  → all fields mapped correctly, calculatedOverallRating is avg of 5 scores
 *  - toDTO()     → all fields mapped correctly including nested hotel/customer/booking IDs
 */
@DisplayName("ReviewMapper — Unit Tests")
class ReviewMapperTest {

    // ── Shared fixtures ───────────────────────────────────────────────────────
    private UUID reviewId;
    private UUID hotelId;
    private UUID customerId;
    private UUID bookingId;

    private Hotel    hotel;
    private Customer customer;
    private Booking  booking;

    private ReviewRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        reviewId   = UUID.randomUUID();
        hotelId    = UUID.randomUUID();
        customerId = UUID.randomUUID();
        bookingId  = UUID.randomUUID();

        // Hotel stub
        hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Grand Azure Hotel");

        // User stub (for customer.getUser().getName())
        User user = new User();
        user.setName("Ahmed Mansour");

        // Customer stub
        customer = new Customer();
        customer.setId(customerId);
        customer.setUser(user);

        // Booking stub
        booking = new Booking();
        booking.setId(bookingId);

        // RequestDTO
        requestDTO = new ReviewRequestDTO();
        requestDTO.setHotelId(hotelId);
        requestDTO.setCustomerId(customerId);
        requestDTO.setBookingId(bookingId);
        requestDTO.setCleanlinessScore(8);
        requestDTO.setLocationScore(9);
        requestDTO.setValueScore(7);
        requestDTO.setComfortScore(8);
        requestDTO.setServiceScore(9);
        requestDTO.setCustomerOverallRating(8);
        requestDTO.setTitle("Great stay!");
        requestDTO.setComment("Loved the infinity pool.");
        requestDTO.setTravelType(Review.TravelType.COUPLE);
    }

    // =========================================================================
    // toEntity()
    // =========================================================================
    @Nested
    @DisplayName("toEntity()")
    class ToEntity {

        @Test
        @DisplayName("Maps hotel, customer, and booking correctly")
        void toEntity_mapsRelationshipsCorrectly() {
            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            assertThat(entity.getHotel()).isSameAs(hotel);
            assertThat(entity.getCustomer()).isSameAs(customer);
            assertThat(entity.getBooking()).isSameAs(booking);
        }

        @Test
        @DisplayName("Maps all score fields from DTO")
        void toEntity_mapsScoreFieldsCorrectly() {
            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            assertThat(entity.getCleanlinessScore()).isEqualTo(8);
            assertThat(entity.getLocationScore()).isEqualTo(9);
            assertThat(entity.getValueScore()).isEqualTo(7);
            assertThat(entity.getComfortScore()).isEqualTo(8);
            assertThat(entity.getServiceScore()).isEqualTo(9);
            assertThat(entity.getCustomerOverallRating()).isEqualTo(8);
        }

        @Test
        @DisplayName("Maps title, comment, and travelType correctly")
        void toEntity_mapsTextAndEnumFields() {
            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            assertThat(entity.getTitle()).isEqualTo("Great stay!");
            assertThat(entity.getComment()).isEqualTo("Loved the infinity pool.");
            assertThat(entity.getTravelType()).isEqualTo(Review.TravelType.COUPLE);
        }

        @Test
        @DisplayName("CalculatedOverallRating = (8+9+7+8+9) / 5 = 8.2")
        void toEntity_calculatesCorrectAverageRating() {
            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            // (8 + 9 + 7 + 8 + 9) / 5.0 = 41 / 5.0 = 8.2
            assertThat(entity.getCalculatedOverallRating()).isEqualTo(8.2);
        }

        @Test
        @DisplayName("All equal scores → calculatedOverallRating equals that score")
        void toEntity_allEqualScores_calculatedRatingEqualsScore() {
            requestDTO.setCleanlinessScore(5);
            requestDTO.setLocationScore(5);
            requestDTO.setValueScore(5);
            requestDTO.setComfortScore(5);
            requestDTO.setServiceScore(5);

            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            assertThat(entity.getCalculatedOverallRating()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("All max scores (10) → calculatedOverallRating = 10.0")
        void toEntity_maxScores_calculatedRatingIsMax() {
            requestDTO.setCleanlinessScore(10);
            requestDTO.setLocationScore(10);
            requestDTO.setValueScore(10);
            requestDTO.setComfortScore(10);
            requestDTO.setServiceScore(10);

            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            assertThat(entity.getCalculatedOverallRating()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("All min scores (1) → calculatedOverallRating = 1.0")
        void toEntity_minScores_calculatedRatingIsMin() {
            requestDTO.setCleanlinessScore(1);
            requestDTO.setLocationScore(1);
            requestDTO.setValueScore(1);
            requestDTO.setComfortScore(1);
            requestDTO.setServiceScore(1);

            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            assertThat(entity.getCalculatedOverallRating()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Null title and comment are mapped as null (optional fields)")
        void toEntity_nullOptionalFields_mappedAsNull() {
            requestDTO.setTitle(null);
            requestDTO.setComment(null);
            requestDTO.setTravelType(null);

            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            assertThat(entity.getTitle()).isNull();
            assertThat(entity.getComment()).isNull();
            assertThat(entity.getTravelType()).isNull();
        }

        @Test
        @DisplayName("Does not pre-set flagged or hidden (defaults expected from entity)")
        void toEntity_doesNotPreSetFlaggedOrHidden() {
            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            // Entity defaults: isFlagged=false, isHidden=false
            assertThat(entity.isFlagged()).isFalse();
            assertThat(entity.isHidden()).isFalse();
        }

        @Test
        @DisplayName("Does not pre-set managerReply or repliedAt")
        void toEntity_doesNotPreSetReplyFields() {
            Review entity = ReviewMapper.toEntity(requestDTO, hotel, customer, booking);

            assertThat(entity.getManagerReply()).isNull();
            assertThat(entity.getRepliedAt()).isNull();
        }
    }

    // =========================================================================
    // toDTO()
    // =========================================================================
    @Nested
    @DisplayName("toDTO()")
    class ToDTO {

        private Review buildFullReview() {
            Review r = new Review();
            r.setId(reviewId);
            r.setHotel(hotel);
            r.setCustomer(customer);
            r.setBooking(booking);
            r.setCleanlinessScore(8);
            r.setLocationScore(9);
            r.setValueScore(7);
            r.setComfortScore(8);
            r.setServiceScore(9);
            r.setCustomerOverallRating(8);
            r.setCalculatedOverallRating(8.2);
            r.setTitle("Great stay!");
            r.setComment("Loved the infinity pool.");
            r.setTravelType(Review.TravelType.COUPLE);
            r.setManagerReply(null);
            r.setRepliedAt(null);
            r.setFlagged(false);
            r.setHidden(false);
            r.setCreatedAt(LocalDateTime.of(2024, 3, 10, 12, 0));
            return r;
        }

        @Test
        @DisplayName("Maps review ID correctly")
        void toDTO_mapsId() {
            ReviewResponseDTO dto = ReviewMapper.toDTO(buildFullReview());
            assertThat(dto.getId()).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("Maps hotel ID and hotel name from nested hotel entity")
        void toDTO_mapsHotelFields() {
            ReviewResponseDTO dto = ReviewMapper.toDTO(buildFullReview());
            assertThat(dto.getHotelId()).isEqualTo(hotelId);
            assertThat(dto.getHotelName()).isEqualTo("Grand Azure Hotel");
        }

        @Test
        @DisplayName("Maps customer ID and customer name from nested user entity")
        void toDTO_mapsCustomerFields() {
            ReviewResponseDTO dto = ReviewMapper.toDTO(buildFullReview());
            assertThat(dto.getCustomerId()).isEqualTo(customerId);
            assertThat(dto.getCustomerName()).isEqualTo("Ahmed Mansour");
        }

        @Test
        @DisplayName("Maps booking ID from nested booking entity")
        void toDTO_mapsBookingId() {
            ReviewResponseDTO dto = ReviewMapper.toDTO(buildFullReview());
            assertThat(dto.getBookingId()).isEqualTo(bookingId);
        }

        @Test
        @DisplayName("Maps all score fields")
        void toDTO_mapsScoreFields() {
            ReviewResponseDTO dto = ReviewMapper.toDTO(buildFullReview());
            assertThat(dto.getCleanlinessScore()).isEqualTo(8);
            assertThat(dto.getLocationScore()).isEqualTo(9);
            assertThat(dto.getValueScore()).isEqualTo(7);
            assertThat(dto.getComfortScore()).isEqualTo(8);
            assertThat(dto.getServiceScore()).isEqualTo(9);
            assertThat(dto.getCustomerOverallRating()).isEqualTo(8);
            assertThat(dto.getCalculatedOverallRating()).isEqualTo(8.2);
        }

        @Test
        @DisplayName("Maps title, comment, and travelType")
        void toDTO_mapsTextFields() {
            ReviewResponseDTO dto = ReviewMapper.toDTO(buildFullReview());
            assertThat(dto.getTitle()).isEqualTo("Great stay!");
            assertThat(dto.getComment()).isEqualTo("Loved the infinity pool.");
            assertThat(dto.getTravelType()).isEqualTo(Review.TravelType.COUPLE);
        }

        @Test
        @DisplayName("Maps flagged and hidden boolean flags")
        void toDTO_mapsBooleanFlags() {
            Review r = buildFullReview();
            r.setFlagged(true);
            r.setHidden(false);

            ReviewResponseDTO dto = ReviewMapper.toDTO(r);
            assertThat(dto.isFlagged()).isTrue();
            assertThat(dto.isHidden()).isFalse();
        }

        @Test
        @DisplayName("Maps manager reply and repliedAt when set")
        void toDTO_mapsManagerReplyFields() {
            Review r = buildFullReview();
            LocalDateTime repliedAt = LocalDateTime.of(2024, 3, 11, 9, 0);
            r.setManagerReply("Thank you for your review!");
            r.setRepliedAt(repliedAt);

            ReviewResponseDTO dto = ReviewMapper.toDTO(r);
            assertThat(dto.getManagerReply()).isEqualTo("Thank you for your review!");
            assertThat(dto.getRepliedAt()).isEqualTo(repliedAt);
        }

        @Test
        @DisplayName("Maps createdAt timestamp")
        void toDTO_mapsCreatedAt() {
            ReviewResponseDTO dto = ReviewMapper.toDTO(buildFullReview());
            assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 3, 10, 12, 0));
        }

        @Test
        @DisplayName("Null managerReply and repliedAt remain null in DTO")
        void toDTO_nullReplyFields_remainNull() {
            ReviewResponseDTO dto = ReviewMapper.toDTO(buildFullReview());
            assertThat(dto.getManagerReply()).isNull();
            assertThat(dto.getRepliedAt()).isNull();
        }

        @Test
        @DisplayName("All TravelType enum values map through correctly")
        void toDTO_allTravelTypes_mapCorrectly() {
            for (Review.TravelType type : Review.TravelType.values()) {
                Review r = buildFullReview();
                r.setTravelType(type);

                ReviewResponseDTO dto = ReviewMapper.toDTO(r);
                assertThat(dto.getTravelType()).isEqualTo(type);
            }
        }
    }
}

