package com.HotelBook.HotelBooking.catalog.amenity;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HotelAmenityMapper {

    public HotelAmenityResponse toResponse(HotelAmenity amenity) {
        return HotelAmenityResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .category(amenity.getCategory())
                .icon(amenity.getIcon())
                .build();
    }

    public List<HotelAmenityResponse> toResponseList(List<HotelAmenity> amenities) {
        return amenities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
