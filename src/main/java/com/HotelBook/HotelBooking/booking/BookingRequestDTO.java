package com.HotelBook.HotelBooking.booking;


import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public class BookingRequestDTO {

    @NotNull(message = "Hotel ID is required")
    private UUID hotelId;

    @NotNull(message = "Room ID is required")
    private UUID roomId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least 1 adult is required")
    @Max(value = 20, message = "Cannot book for more than 20 adults")
    private Integer adults;

    @Min(value = 0, message = "Children cannot be negative")
    @Max(value = 10, message = "Cannot book for more than 10 children")
    private Integer children = 0;

    @Min(value = 1, message = "Must book at least 1 room")
    @Max(value = 10, message = "Cannot book more than 10 rooms at once")
    private Integer roomCount = 1;

    private UUID cancellationPolicyId;

   @Size(max = 1000, message = "Special requests cannot exceed 1000 characters")
    private String specialRequests;


    public UUID getHotelId() { return hotelId; }
    public void setHotelId(UUID hotelId) { this.hotelId = hotelId; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getAdults() { return adults; }
    public void setAdults(Integer adults) { this.adults = adults; }

    public Integer getChildren() { return children; }
    public void setChildren(Integer children) { this.children = children; }

    public Integer getRoomCount() { return roomCount; }
    public void setRoomCount(Integer roomCount) { this.roomCount = roomCount; }

    public UUID getCancellationPolicyId() { return cancellationPolicyId; }
    public void setCancellationPolicyId(UUID cancellationPolicyId) { this.cancellationPolicyId = cancellationPolicyId; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
}
