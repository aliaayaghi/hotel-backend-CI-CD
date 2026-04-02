package com.HotelBook.HotelBooking.catalog.nearby;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NearbyPlaceMapper {

    public NearbyPlaceResponse toResponse(NearbyPlace place) {
        return NearbyPlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .type(place.getType())
                .distanceKm(place.getDistanceKm())
                .build();
    }

    public List<NearbyPlaceResponse> toResponseList(List<NearbyPlace> places) {
        return places.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
