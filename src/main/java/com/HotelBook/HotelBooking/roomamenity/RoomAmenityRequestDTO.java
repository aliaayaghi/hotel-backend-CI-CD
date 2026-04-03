package com.HotelBook.HotelBooking.roomamenity;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class RoomAmenityRequestDTO {

    @NotBlank(message = "Amenity name is required")
    @Size(max = 100, message = "Amenity name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Category is required")
    private AmenityCategory category;  // COMFORT | TECH | BATHROOM | KITCHEN | ACCESSIBILITY

    @Size(max = 100, message = "Icon key cannot exceed 100 characters")
    private String icon;  // optional — frontend icon key e.g. "wifi", "ac", "tv"

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AmenityCategory getCategory() { return category; }
    public void setCategory(AmenityCategory category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
