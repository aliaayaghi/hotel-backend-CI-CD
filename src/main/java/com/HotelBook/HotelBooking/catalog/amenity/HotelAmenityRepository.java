package com.HotelBook.HotelBooking.catalog.amenity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, UUID> {

    // ── Core queries ───────────────────────────────────────────────────────────

    /** All amenities for a hotel — used to build HotelDetailResponse */
    List<HotelAmenity> findByHotelId(UUID hotelId);

    /** Filter amenities by category — used by frontend category tabs */
    List<HotelAmenity> findByHotelIdAndCategory(UUID hotelId, AmenityCategory category);

    /** Duplicate guard — prevents adding "Outdoor Pool" twice to the same hotel */
    boolean existsByHotelIdAndNameIgnoreCase(UUID hotelId, String name);

    /** Count for summary/dashboard */
    long countByHotelId(UUID hotelId);
}
