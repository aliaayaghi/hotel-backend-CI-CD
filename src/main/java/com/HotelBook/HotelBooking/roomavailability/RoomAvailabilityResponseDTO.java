package com.HotelBook.HotelBooking.roomavailability;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


public class RoomAvailabilityResponseDTO {

    /** PK of this availability record. */
    private UUID id;

    /** FK to the room. */
    private UUID roomId;

    /** The specific calendar date that is blocked. */
    private LocalDate date;


    private Integer blockedCount;


    private String blockedReason;


    private UUID bookingId;


    private LocalDateTime createdAt;


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getBlockedCount() { return blockedCount; }
    public void setBlockedCount(Integer blockedCount) { this.blockedCount = blockedCount; }

    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
