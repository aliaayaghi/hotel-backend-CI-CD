package com.HotelBook.HotelBooking.catalog.photo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotelPhotoRepository extends JpaRepository<HotelPhoto, UUID> {

    /**
     * Returns all photos for a hotel in ascending display order.
     * Used by getPhotos() and also by HotelServiceImpl when building HotelDetailResponse.
     */
    List<HotelPhoto> findByHotelIdOrderByOrderAsc(UUID hotelId);

    /**
     * Returns all photos for a hotel without ordering.
     * Used by reorderPhotos() to fetch all before reassigning order values.
     */
    List<HotelPhoto> findByHotelId(UUID hotelId);

    /**
     * Returns the current cover photo, if one exists.
     * Used in addPhoto() to clear isCover before setting a new cover.
     */
    Optional<HotelPhoto> findFirstByHotelIdAndIsCoverTrue(UUID hotelId);

    /**
     * Counts photos for a hotel.
     * Used to set the initial order when adding a new non-cover photo.
     */
    long countByHotelId(UUID hotelId);

    /**
     * Checks whether a specific photo belongs to a given hotel.
     * Used to validate ownership before delete operations.
     */
    boolean existsByIdAndHotelId(UUID photoId, UUID hotelId);

    /**
     * Bulk-clears the isCover flag for all photos of a hotel.
     * Called before setting a new cover to ensure exactly one cover exists.
     *
     * @Modifying requires @Transactional on the calling method.
     */
    @Modifying
    @Query("UPDATE HotelPhoto p SET p.isCover = false WHERE p.hotelId = :hotelId")
    void clearCoverByHotelId(@Param("hotelId") UUID hotelId);
}
