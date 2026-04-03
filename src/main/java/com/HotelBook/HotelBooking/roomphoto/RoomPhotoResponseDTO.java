package com.HotelBook.HotelBooking.roomphoto;



import java.time.LocalDateTime;
import java.util.UUID;


public class RoomPhotoResponseDTO {

    private UUID id;
    private UUID roomId;
    private String url;
    private Integer displayOrder;
    private String caption;
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
