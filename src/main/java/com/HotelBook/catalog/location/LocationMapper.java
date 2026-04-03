package com.HotelBook.catalog.location;

import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public LocationResponse toLocationResponse(Location location) {
        return LocationResponse.builder()
                .hotelId(location.getId())
                .country(location.getCountry())
                .city(location.getCity())
                .state(location.getState())
                .address(location.getAddress())
                .zipCode(location.getZipCode())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .googleMapsPlaceId(location.getGoogleMapsPlaceId())
                .updatedAt(location.getUpdatedAt())
                .build();
    }

    public NearbyHotelResponse toNearbyHotelResponse(Location location, double distanceKm) {
        String hotelName = location.getHotel() != null ? location.getHotel().getName() : null;

        return NearbyHotelResponse.builder()
                .hotelId(location.getId())
                .hotelName(hotelName)
                .country(location.getCountry())
                .city(location.getCity())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .googleMapsPlaceId(location.getGoogleMapsPlaceId())
                .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                .build();
    }
}
