package com.HotelBook.HotelBooking.catalog.amenity;

import java.util.List;
import java.util.UUID;

public interface HotelAmenityService {

    /**
     * Get all amenities for a hotel, ordered by category.
     * Public endpoint — no authentication required.
     */
    List<HotelAmenityResponse> getAmenities(UUID hotelId);

    /**
     * Get amenities filtered by a specific category.
     * Useful for the frontend's category-tab view.
     */
    List<HotelAmenityResponse> getAmenitiesByCategory(UUID hotelId, AmenityCategory category);

    /**
     * Add a new amenity to a hotel.
     * Only the hotel's owner (HOTEL_MANAGER) can call this.
     * Throws DuplicateAmenityException if same name already exists for this hotel.
     *
     * @param hotelId  the hotel to add the amenity to
     * @param managerId the authenticated manager — used to validate ownership
     * @param request  amenity details
     */
    HotelAmenityResponse addAmenity(UUID hotelId, UUID managerId, CreateAmenityRequest request);

    /**
     * Remove an amenity from a hotel.
     * Only the hotel's owner (HOTEL_MANAGER) or ADMIN can call this.
     *
     * @param hotelId   the hotel that owns this amenity
     * @param amenityId the amenity to delete
     * @param managerId the authenticated manager — used to validate ownership
     */
    void removeAmenity(UUID hotelId, UUID amenityId, UUID managerId);
}