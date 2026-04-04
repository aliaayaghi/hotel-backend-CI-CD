package com.HotelBook.HotelBooking.Review.Entity;


import com.HotelBook.HotelBooking.booking.Booking;
import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.catalog.user.entity.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer cleanlinessScore;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer locationScore;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer valueScore;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer comfortScore;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer serviceScore;

    @Min(1) @Max(10)
    @Column(nullable = false)
    private Integer customerOverallRating;

    private Double calculatedOverallRating;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime repliedAt;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    private TravelType travelType;

    @Column(columnDefinition = "TEXT")
    private String managerReply;

    @Column(nullable = false)
    private boolean isFlagged = false;

    @Column(nullable = false)
    private boolean isHidden = false;

    public enum TravelType {
        SOLO, COUPLE, FAMILY, BUSINESS, GROUP
    }

    // Constructors
    public Review() {}

    public Review(Hotel hotel, Customer customer, Booking booking,
                  Integer cleanlinessScore, Integer locationScore, Integer valueScore,
                  Integer comfortScore, Integer serviceScore, Integer customerOverallRating) {
        this.hotel = hotel;
        this.customer = customer;
        this.booking = booking;
        this.cleanlinessScore = cleanlinessScore;
        this.locationScore = locationScore;
        this.valueScore = valueScore;
        this.comfortScore = comfortScore;
        this.serviceScore = serviceScore;
        this.customerOverallRating = customerOverallRating;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

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

    public Double getCalculatedOverallRating() { return calculatedOverallRating; }
    public void setCalculatedOverallRating(Double calculatedOverallRating) { this.calculatedOverallRating = calculatedOverallRating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public TravelType getTravelType() { return travelType; }
    public void setTravelType(TravelType travelType) { this.travelType = travelType; }

    public String getManagerReply() { return managerReply; }
    public void setManagerReply(String managerReply) { this.managerReply = managerReply; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review)) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", hotelId=" + (hotel != null ? hotel.getId() : null) +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", customerOverallRating=" + customerOverallRating +
                ", calculatedOverallRating=" + calculatedOverallRating +
                '}';
    }
}