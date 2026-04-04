package com.HotelBook.HotelBooking.Review.repository;



import com.HotelBook.HotelBooking.Review.Entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    // Find reviews by hotel
    Page<Review> findByHotelId(UUID hotelId, Pageable pageable);

    // Find reviews by customer
    Page<Review> findByCustomerId(UUID customerId, Pageable pageable);

    // Check if a customer already reviewed a booking (prevent duplicate reviews)
    boolean existsByBookingId(UUID bookingId);

    // Find review by booking ID
    Optional<Review> findByBookingId(UUID bookingId);

    // Count reviews for a hotel
    long countByHotelId(UUID hotelId);

    // Get average rating for a hotel (calculated from all review scores)
    @Query("SELECT AVG(r.calculatedOverallRating) FROM Review r WHERE r.hotel.id = :hotelId AND r.isHidden = false")
    Double getAverageRatingForHotel(@Param("hotelId") UUID hotelId);

    // Get average of each sub-score for a hotel
    @Query("SELECT AVG(r.cleanlinessScore) FROM Review r WHERE r.hotel.id = :hotelId AND r.isHidden = false")
    Double getAverageCleanlinessScore(@Param("hotelId") UUID hotelId);

    @Query("SELECT AVG(r.locationScore) FROM Review r WHERE r.hotel.id = :hotelId AND r.isHidden = false")
    Double getAverageLocationScore(@Param("hotelId") UUID hotelId);

    @Query("SELECT AVG(r.serviceScore) FROM Review r WHERE r.hotel.id = :hotelId AND r.isHidden = false")
    Double getAverageServiceScore(@Param("hotelId") UUID hotelId);

    @Query("SELECT AVG(r.valueScore) FROM Review r WHERE r.hotel.id = :hotelId AND r.isHidden = false")
    Double getAverageValueScore(@Param("hotelId") UUID hotelId);

    @Query("SELECT AVG(r.comfortScore) FROM Review r WHERE r.hotel.id = :hotelId AND r.isHidden = false")
    Double getAverageComfortScore(@Param("hotelId") UUID hotelId);
}