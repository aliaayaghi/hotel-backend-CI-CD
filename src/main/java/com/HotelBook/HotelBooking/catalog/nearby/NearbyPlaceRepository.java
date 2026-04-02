package com.HotelBook.HotelBooking.catalog.nearby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NearbyPlaceRepository extends JpaRepository<NearbyPlace, UUID> {

    /** All nearby places for a hotel — used to build HotelDetailResponse */
    List<NearbyPlace> findByHotelId(UUID hotelId);

    /** Filter by type — e.g. all beaches near the hotel */
    List<NearbyPlace> findByHotelIdAndType(UUID hotelId, NearbyPlaceType type);

    /** Sorted by distance ascending — closest places first */
    List<NearbyPlace> findByHotelIdOrderByDistanceKmAsc(UUID hotelId);
}
