package com.HotelBook.HotelBooking.catalog.nearby;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class NearbyPlaceResponse {

    private UUID id;
    private String name;
    private NearbyPlaceType type;
    private BigDecimal distanceKm;
}
