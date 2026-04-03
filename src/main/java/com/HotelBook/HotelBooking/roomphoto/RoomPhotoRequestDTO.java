package com.HotelBook.HotelBooking.roomphoto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class RoomPhotoRequestDTO {

    @NotBlank(message = "Photo URL is required")
    @Size(max = 500, message = "URL cannot exceed 500 characters")
    private String url;

    private Integer displayOrder;

    @Size(max = 255, message = "Caption cannot exceed 255 characters")
    private String caption;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
