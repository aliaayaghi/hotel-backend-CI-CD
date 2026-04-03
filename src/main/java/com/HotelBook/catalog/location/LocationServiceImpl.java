package com.HotelBook.catalog.location;

import com.HotelBook.catalog.hotel.Hotel;
import com.HotelBook.catalog.hotel.HotelRepository;
import com.HotelBook.catalog.user.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final HotelRepository hotelRepository;
    private final LocationMapper locationMapper;

    // Earth's mean radius in km — used by Haversine formula
    private static final double EARTH_RADIUS_KM = 6371.0;

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LocationResponse createLocation(UUID hotelId, CreateLocationRequest request) {
        // Verify the hotel exists
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        // A hotel can only have one location record
        if (locationRepository.existsByHotel_Id(hotelId)) {
            throw new LocationAlreadyExistsException(hotelId);
        }

        Location location = Location.builder()
                .hotel(hotel)
                .country(request.getCountry())
                .city(request.getCity())
                .state(request.getState())
                .address(request.getAddress())
                .zipCode(request.getZipCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .googleMapsPlaceId(request.getGoogleMapsPlaceId())
                .build();

        location = locationRepository.save(location);
        log.info("Created location for hotel {} — {}, {}", hotelId, request.getCity(), request.getCountry());
        return locationMapper.toLocationResponse(location);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLocationByHotelId(UUID hotelId) {
        Location location = findByHotelId(hotelId);
        return locationMapper.toLocationResponse(location);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LocationResponse updateLocation(UUID hotelId, UpdateLocationRequest request) {
        Location location = findByHotelId(hotelId);

        // Patch — only update non-null fields sent by the client
        if (request.getCountry() != null)          location.setCountry(request.getCountry());
        if (request.getCity() != null)             location.setCity(request.getCity());
        if (request.getState() != null)            location.setState(request.getState());
        if (request.getAddress() != null)          location.setAddress(request.getAddress());
        if (request.getZipCode() != null)          location.setZipCode(request.getZipCode());
        if (request.getLatitude() != null)         location.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)        location.setLongitude(request.getLongitude());
        if (request.getGoogleMapsPlaceId() != null) location.setGoogleMapsPlaceId(request.getGoogleMapsPlaceId());

        location = locationRepository.save(location);
        log.info("Updated location for hotel {}", hotelId);
        return locationMapper.toLocationResponse(location);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteLocation(UUID hotelId) {
        Location location = findByHotelId(hotelId);
        locationRepository.delete(location);
        log.info("Deleted location for hotel {}", hotelId);
    }

    // ── Proximity search ──────────────────────────────────────────────────────

    /**
     * Find all hotels within {@code radiusKm} kilometres of ({@code lat}, {@code lng}).
     *
     * Algorithm:
     *  1. Convert the radius to a lat/lng bounding box (cheap rectangle filter).
     *  2. Fetch all locations inside the bounding box from the DB — fast index scan.
     *  3. Apply the Haversine formula to compute the exact great-circle distance
     *     and discard anything outside the true circle radius.
     *  4. Sort results by distance ascending (closest first).
     *
     * Why two steps?
     *  The bounding-box query is cheap (uses index on lat/lng columns).
     *  Haversine is computed in Java only on the small candidate set that
     *  survived the bounding box — avoids a full-table scan.
     */
    @Override
    @Transactional(readOnly = true)
    public List<NearbyHotelResponse> findNearby(double lat, double lng, double radiusKm) {
        // ── Step 1: bounding box ──────────────────────────────────────────────
        // 1 degree of latitude ≈ 111 km everywhere
        // 1 degree of longitude ≈ 111 km × cos(lat) — shrinks near the poles
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        List<Location> candidates = locationRepository.findWithinBoundingBox(
                lat - latDelta, lat + latDelta,
                lng - lngDelta, lng + lngDelta
        );

        // ── Step 2: Haversine exact filter + sort ─────────────────────────────
        return candidates.stream()
                .map(loc -> {
                    double dist = haversineKm(lat, lng, loc.getLatitude(), loc.getLongitude());
                    return new AbstractMap.SimpleEntry<>(loc, dist);
                })
                .filter(entry -> entry.getValue() <= radiusKm)
                .sorted(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue))
                .map(entry -> locationMapper.toNearbyHotelResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Location findByHotelId(UUID hotelId) {
        return locationRepository.findByHotel_Id(hotelId)
                .orElseThrow(() -> new LocationNotFoundException(hotelId));
    }

    /**
     * Haversine formula — great-circle distance between two lat/lng points.
     *
     * @return distance in kilometres
     */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
