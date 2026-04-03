package com.HotelBook.HotelBooking.roomaccessibility;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class RoomAccessibilityRequestDTO {

    @NotBlank(message = "Feature description is required")
    @Size(max = 255, message = "Feature description cannot exceed 255 characters")
    private String feature;

    @NotNull(message = "isAvailable is required — use true or false")
    private Boolean isAvailable;

    public String getFeature() { return feature; }
    public void setFeature(String feature) { this.feature = feature; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
