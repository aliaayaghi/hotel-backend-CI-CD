package com.HotelBook.HotelBooking.catalog.nearby;

import com.HotelBook.HotelBooking.catalog.hotel.HotelNotFoundException;
import com.HotelBook.HotelBooking.catalog.hotel.UnauthorizedHotelAccessException;
import com.HotelBook.HotelBooking.catalog.hotel.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NearbyPlaceServiceImpl implements NearbyPlaceService {

    private final NearbyPlaceRepository nearbyPlaceRepository;
    private final HotelRepository hotelRepository;
    private final NearbyPlaceMapper nearbyPlaceMapper;

    // ── Get all nearby places ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NearbyPlaceResponse> getNearbyPlaces(UUID hotelId) {
        verifyHotelExists(hotelId);
        // Return sorted by distance ascending — closest first
        List<NearbyPlace> places = nearbyPlaceRepository.findByHotelIdOrderByDistanceKmAsc(hotelId);
        return nearbyPlaceMapper.toResponseList(places);
    }

    // ── Get nearby places filtered by type ────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NearbyPlaceResponse> getNearbyPlacesByType(UUID hotelId, NearbyPlaceType type) {
        verifyHotelExists(hotelId);
        List<NearbyPlace> places = nearbyPlaceRepository.findByHotelIdAndType(hotelId, type);
        return nearbyPlaceMapper.toResponseList(places);
    }

    // ── Add nearby place ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public NearbyPlaceResponse addNearbyPlace(UUID hotelId, UUID managerId, CreateNearbyPlaceRequest request) {
        // 1. Verify hotel exists
        verifyHotelExists(hotelId);

        // 2. Verify manager owns this hotel
        validateManagerOwnership(managerId, hotelId);

        // 3. Build and save
        NearbyPlace place = NearbyPlace.builder()
                .hotelId(hotelId)
                .name(request.getName())
                .type(request.getType())
                .distanceKm(request.getDistanceKm())
                .build();

        place = nearbyPlaceRepository.save(place);
        log.info("Added nearby place '{}' ({}, {}km) to hotel {}",
                place.getName(), place.getType(), place.getDistanceKm(), hotelId);

        return nearbyPlaceMapper.toResponse(place);
    }

    // ── Remove nearby place ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void removeNearbyPlace(UUID hotelId, UUID placeId, UUID managerId) {
        // 1. Fetch the place
        NearbyPlace place = nearbyPlaceRepository.findById(placeId)
                .orElseThrow(() -> new HotelNotFoundException(placeId));

        // 2. Verify it belongs to the specified hotel (prevents cross-hotel deletion)
        if (!place.getHotelId().equals(hotelId)) {
            throw new HotelNotFoundException(placeId);
        }

        // 3. Verify manager owns this hotel
        validateManagerOwnership(managerId, hotelId);

        // 4. Delete
        nearbyPlaceRepository.delete(place);
        log.info("Removed nearby place '{}' from hotel {}", place.getName(), hotelId);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void verifyHotelExists(UUID hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException(hotelId);
        }
    }

    private void validateManagerOwnership(UUID managerId, UUID hotelId) {
        if (!hotelRepository.existsByIdAndManager_Id(hotelId, managerId)) {
            throw new UnauthorizedHotelAccessException(hotelId);
        }
    }
}