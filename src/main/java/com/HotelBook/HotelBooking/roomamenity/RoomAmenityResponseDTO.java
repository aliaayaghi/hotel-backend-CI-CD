package com.HotelBook.HotelBooking.roomamenity;



import java.util.UUID;


public class RoomAmenityResponseDTO {

    private UUID id;
    private UUID roomId;
    private String name;
    private String category;  // String so JSON shows "TECH" not an integer
    private String icon;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
