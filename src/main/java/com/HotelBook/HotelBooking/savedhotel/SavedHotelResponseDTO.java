package com.HotelBook.HotelBooking.savedhotel;


import java.time.LocalDateTime;
import java.util.UUID;


public class SavedHotelResponseDTO {

    private UUID id;
    private UUID customerId;
    private UUID hotelId;
    private String notes;
    private LocalDateTime savedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getHotelId() { return hotelId; }
    public void setHotelId(UUID hotelId) { this.hotelId = hotelId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}