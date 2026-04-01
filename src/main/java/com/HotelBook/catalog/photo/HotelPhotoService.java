package com.HotelBook.catalog.photo;

import java.util.List;
import java.util.UUID;

public interface HotelPhotoService {

    /**
     * Returns all photos for a hotel sorted by display_order ascending.
     * Public — no auth required.
     */
    List<HotelPhotoResponse> getPhotos(UUID hotelId);

    /**
     * Adds a new photo to a hotel.
     * If isCover=true, clears the existing cover first.
     * If isCover=false, the new photo's order is set to photoCount (appended last).
     *
     * Requires: authenticated HOTEL_MANAGER who owns the hotel.
     */
    HotelPhotoResponse addPhoto(UUID hotelId, UUID managerId, CreatePhotoRequest request);

    /**
     * Deletes a single photo by ID.
     * Validates that the photo belongs to the given hotelId.
     *
     * Requires: authenticated HOTEL_MANAGER who owns the hotel.
     */
    void deletePhoto(UUID hotelId, UUID photoId, UUID managerId);

    /**
     * Reassigns display_order for all photos of a hotel.
     * The position of each UUID in request.photoIds becomes that photo's new order value.
     *
     * All existing photo IDs for the hotel must be included.
     * Requires: authenticated HOTEL_MANAGER who owns the hotel.
     */
    List<HotelPhotoResponse> reorderPhotos(UUID hotelId, UUID managerId, ReorderPhotosRequest request);
}