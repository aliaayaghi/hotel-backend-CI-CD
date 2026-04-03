package com.HotelBook.HotelBooking.catalog.hotel;

import org.springframework.stereotype.Component;

@Component
public class HotelMapper {

    public HotelResponse toHotelResponse(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .type(hotel.getType())
                .overview(hotel.getOverview())
                .starRating(hotel.getStarRating())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .countryCode(hotel.getCountryCode())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .phone(hotel.getPhone())
                .email(hotel.getEmail())
                .website(hotel.getWebsite())
                .managerId(hotel.getManager() != null ? hotel.getManager().getId() : null)
                .managerName(hotel.getManager() != null && hotel.getManager().getUser() != null
                        ? hotel.getManager().getUser().getName() : null)
                .status(hotel.getStatus())
                .rejectionReason(hotel.getRejectionReason())
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .build();
    }

    public HotelSummaryResponse toHotelSummaryResponse(Hotel hotel) {
        return HotelSummaryResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .type(hotel.getType())
                .starRating(hotel.getStarRating())
                .city(hotel.getCity())
                .countryCode(hotel.getCountryCode())
                .address(hotel.getAddress())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .build();
    }
}
