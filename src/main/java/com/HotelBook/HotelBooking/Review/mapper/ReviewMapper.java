package com.HotelBook.HotelBooking.Review.mapper;


import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.booking.Booking;
import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.catalog.user.entity.Customer;

public class ReviewMapper {

    private ReviewMapper() {}

    public static Review toEntity(ReviewRequestDTO dto, Hotel hotel, Customer customer, Booking booking) {
        Review review = new Review();
        review.setHotel(hotel);
        review.setCustomer(customer);
        review.setBooking(booking);
        review.setCleanlinessScore(dto.getCleanlinessScore());
        review.setLocationScore(dto.getLocationScore());
        review.setValueScore(dto.getValueScore());
        review.setComfortScore(dto.getComfortScore());
        review.setServiceScore(dto.getServiceScore());
        review.setCustomerOverallRating(dto.getCustomerOverallRating());
        review.setTitle(dto.getTitle());
        review.setComment(dto.getComment());
        review.setTravelType(dto.getTravelType());

        // Calculate the average of sub-scores
        double avg = (dto.getCleanlinessScore() + dto.getLocationScore() +
                dto.getValueScore() + dto.getComfortScore() + dto.getServiceScore()) / 5.0;
        review.setCalculatedOverallRating(avg);

        return review;
    }

    public static ReviewResponseDTO toDTO(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getHotel().getId(),
                review.getHotel().getName(),
                review.getCustomer().getId(),
                review.getCustomer().getUser().getName(),
                review.getBooking().getId(),
                review.getCleanlinessScore(),
                review.getLocationScore(),
                review.getValueScore(),
                review.getComfortScore(),
                review.getServiceScore(),
                review.getCustomerOverallRating(),
                review.getCalculatedOverallRating(),
                review.getTitle(),
                review.getComment(),
                review.getTravelType(),
                review.getManagerReply(),
                review.getRepliedAt(),
                review.isFlagged(),
                review.isHidden(),
                review.getCreatedAt()
        );
    }
}