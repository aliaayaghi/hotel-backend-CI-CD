package com.HotelBook.HotelBooking.catalog.amenity;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder

public class HotelAmenityResponse{
    private UUID id;
    private String name;
    private AmenityCategory category;
    private String icon;
}
