package com.HotelBook.HotelBooking.Review.dto;



import com.HotelBook.HotelBooking.Review.Entity.Review;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewResponseDTO {

    private Long id;
    private UUID hotelId;
    private String hotelName;
    private UUID customerId;
    private String customerName;
    private UUID bookingId;

    private Integer cleanlinessScore;
    private Integer locationScore;
    private Integer valueScore;
    private Integer comfortScore;
    private Integer serviceScore;

    private Integer customerOverallRating;
    private Double calculatedOverallRating;

    private String title;
    private String comment;
    private Review.TravelType travelType;

    private String managerReply;
    private LocalDateTime repliedAt;

    private boolean isFlagged;
    private boolean isHidden;

    private LocalDateTime createdAt;

    // Constructor
    public ReviewResponseDTO(Long id, UUID hotelId, String hotelName, UUID customerId, String customerName,
                             UUID bookingId, Integer cleanlinessScore, Integer locationScore,
                             Integer valueScore, Integer comfortScore, Integer serviceScore,
                             Integer customerOverallRating, Double calculatedOverallRating,
                             String title, String comment, Review.TravelType travelType,
                             String managerReply, LocalDateTime repliedAt,
                             boolean isFlagged, boolean isHidden, LocalDateTime createdAt) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.bookingId = bookingId;
        this.cleanlinessScore = cleanlinessScore;
        this.locationScore = locationScore;
        this.valueScore = valueScore;
        this.comfortScore = comfortScore;
        this.serviceScore = serviceScore;
        this.customerOverallRating = customerOverallRating;
        this.calculatedOverallRating = calculatedOverallRating;
        this.title = title;
        this.comment = comment;
        this.travelType = travelType;
        this.managerReply = managerReply;
        this.repliedAt = repliedAt;
        this.isFlagged = isFlagged;
        this.isHidden = isHidden;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public UUID getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public UUID getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public UUID getBookingId() { return bookingId; }
    public Integer getCleanlinessScore() { return cleanlinessScore; }
    public Integer getLocationScore() { return locationScore; }
    public Integer getValueScore() { return valueScore; }
    public Integer getComfortScore() { return comfortScore; }
    public Integer getServiceScore() { return serviceScore; }
    public Integer getCustomerOverallRating() { return customerOverallRating; }
    public Double getCalculatedOverallRating() { return calculatedOverallRating; }
    public String getTitle() { return title; }
    public String getComment() { return comment; }
    public Review.TravelType getTravelType() { return travelType; }
    public String getManagerReply() { return managerReply; }
    public LocalDateTime getRepliedAt() { return repliedAt; }
    public boolean isFlagged() { return isFlagged; }
    public boolean isHidden() { return isHidden; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}