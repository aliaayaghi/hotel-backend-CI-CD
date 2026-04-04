package com.HotelBook.HotelBooking.Review.service;


import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.common.pagination.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface ReviewService {

    ReviewResponseDTO createReview(ReviewRequestDTO request);

    ReviewResponseDTO getReviewById(Long id);

    PagedResponse<ReviewResponseDTO> listReviews(
            Pageable pageable,
            UUID hotelId,
            UUID customerId,
            Review.TravelType travelType,
            Integer minRating,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore,
            Boolean onlyFlagged
    );

    PagedResponse<ReviewResponseDTO> getReviewsByHotelId(UUID hotelId, Pageable pageable);

    PagedResponse<ReviewResponseDTO> getReviewsByCustomerId(UUID customerId, Pageable pageable);

    ReviewResponseDTO addManagerReply(Long reviewId, String managerReply);

    ReviewResponseDTO flagReview(Long reviewId);

    ReviewResponseDTO hideReview(Long reviewId);

    void deleteReview(Long id);

    Map<String, Double> getAverageScoresForHotel(UUID hotelId);

    Double getAverageRatingForHotel(UUID hotelId);
}