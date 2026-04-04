package com.HotelBook.HotelBooking.Review.dto;


import com.HotelBook.HotelBooking.Review.Entity.Review;
import jakarta.validation.constraints.*;

import java.util.UUID;

public class ReviewRequestDTO {

    @NotNull(message = "Hotel ID is required")
    private UUID hotelId;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    @NotNull(message = "Cleanliness score is required")
    @Min(value = 1, message = "Cleanliness score must be at least 1")
    @Max(value = 10, message = "Cleanliness score must be at most 10")
    private Integer cleanlinessScore;

    @NotNull(message = "Location score is required")
    @Min(value = 1, message = "Location score must be at least 1")
    @Max(value = 10, message = "Location score must be at most 10")
    private Integer locationScore;

    @NotNull(message = "Value score is required")
    @Min(value = 1, message = "Value score must be at least 1")
    @Max(value = 10, message = "Value score must be at most 10")
    private Integer valueScore;

    @NotNull(message = "Comfort score is required")
    @Min(value = 1, message = "Comfort score must be at least 1")
    @Max(value = 10, message = "Comfort score must be at most 10")
    private Integer comfortScore;

    @NotNull(message = "Service score is required")
    @Min(value = 1, message = "Service score must be at least 1")
    @Max(value = 10, message = "Service score must be at most 10")
    private Integer serviceScore;

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Overall rating must be at least 1")
    @Max(value = 10, message = "Overall rating must be at most 10")
    private Integer customerOverallRating;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    private Review.TravelType travelType;

    // Getters and Setters
    public UUID getHotelId() { return hotelId; }
    public void setHotelId(UUID hotelId) { this.hotelId = hotelId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public Integer getCleanlinessScore() { return cleanlinessScore; }
    public void setCleanlinessScore(Integer cleanlinessScore) { this.cleanlinessScore = cleanlinessScore; }

    public Integer getLocationScore() { return locationScore; }
    public void setLocationScore(Integer locationScore) { this.locationScore = locationScore; }

    public Integer getValueScore() { return valueScore; }
    public void setValueScore(Integer valueScore) { this.valueScore = valueScore; }

    public Integer getComfortScore() { return comfortScore; }
    public void setComfortScore(Integer comfortScore) { this.comfortScore = comfortScore; }

    public Integer getServiceScore() { return serviceScore; }
    public void setServiceScore(Integer serviceScore) { this.serviceScore = serviceScore; }

    public Integer getCustomerOverallRating() { return customerOverallRating; }
    public void setCustomerOverallRating(Integer customerOverallRating) { this.customerOverallRating = customerOverallRating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Review.TravelType getTravelType() { return travelType; }
    public void setTravelType(Review.TravelType travelType) { this.travelType = travelType; }
}