package com.HotelBook.HotelBooking.catalog.location;

import java.util.List;
import java.util.UUID;

public interface LocationService {

    /** Create location for a hotel — called by HotelService after hotel creation */
    LocationResponse createLocation(UUID hotelId, CreateLocationRequest request);

    /** Get location for a specific hotel */
    LocationResponse getLocationByHotelId(UUID hotelId);

    /** Update location — manager (owns hotel) or admin only */
    LocationResponse updateLocation(UUID hotelId, UpdateLocationRequest request);

    /** Delete location record — cascades from hotel delete, or called directly */
    void deleteLocation(UUID hotelId);

    /** Search hotels within radiusKm of (lat, lng) */
    List<NearbyHotelResponse> findNearby(double lat, double lng, double radiusKm);
}
