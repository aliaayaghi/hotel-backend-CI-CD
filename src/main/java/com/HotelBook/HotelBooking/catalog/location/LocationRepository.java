package com.HotelBook.HotelBooking.catalog.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    /** Find location by hotel id — used by HotelService and LocationService */
    Optional<Location> findByHotel_Id(UUID hotelId);

    /** Check existence before creating */
    boolean existsByHotel_Id(UUID hotelId);

    /** Find all hotels in a given city (case-insensitive) */
    List<Location> findByCityIgnoreCase(String city);

    /** Find all hotels in a given country (case-insensitive) */
    List<Location> findByCountryIgnoreCase(String country);

    /**
     * Proximity search — find locations within a bounding box around
     * a point (lat, lng) ± delta degrees.
     *
     * A delta of 0.045 ≈ 5 km radius at mid-latitudes.
     * For production replace with a PostGIS ST_DWithin query.
     */
    @Query("""
            SELECT l FROM Location l
            WHERE l.latitude  BETWEEN :minLat AND :maxLat
              AND l.longitude BETWEEN :minLng AND :maxLng
            """)
    List<Location> findWithinBoundingBox(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );
}
