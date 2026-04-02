package com.HotelBook.HotelBooking.catalog.nearby;

import java.util.List;
import java.util.UUID;

public interface NearbyPlaceService {

    /**
     * Get all nearby places for a hotel, sorted by distance ascending.
     * Public endpoint — no authentication required.
     */
    List<NearbyPlaceResponse> getNearbyPlaces(UUID hotelId);

    /**
     * Get nearby places filtered by type (e.g. only BEACH entries).
     * Public endpoint — no authentication required.
     */
    List<NearbyPlaceResponse> getNearbyPlacesByType(UUID hotelId, NearbyPlaceType type);

    /**
     * Add a new nearby place entry to a hotel.
     * Only the hotel's owning manager can add entries.
     *
     * @param hotelId   the hotel to add the entry to
     * @param managerId the authenticated manager — used for ownership check
     * @param request   place details
     */
    NearbyPlaceResponse addNearbyPlace(UUID hotelId, UUID managerId, CreateNearbyPlaceRequest request);

    /**
     * Remove a nearby place entry.
     * Only the hotel's owning manager or an ADMIN can delete.
     *
     * @param hotelId   the hotel that owns this entry
     * @param placeId   the place entry to delete
     * @param managerId the authenticated manager — used for ownership check
     */
    void removeNearbyPlace(UUID hotelId, UUID placeId, UUID managerId);
}
