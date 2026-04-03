package com.HotelBook.HotelBooking.roomaccessibility;



import java.util.UUID;


public class RoomAccessibilityResponseDTO {

    private UUID id;
    private UUID roomId;
    private String feature;
    private Boolean isAvailable;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public String getFeature() { return feature; }
    public void setFeature(String feature) { this.feature = feature; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
