package com.HotelBook.HotelBooking.catalog.amenity;


import com.HotelBook.HotelBooking.catalog.hotel.HotelRepository;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.catalog.user.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelAmenityServiceImpl implements HotelAmenityService {

    private final HotelAmenityRepository amenityRepository;
    private final HotelRepository hotelRepository;
    private final HotelAmenityMapper amenityMapper;

    // ── Get all amenities ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<HotelAmenityResponse> getAmenities(UUID hotelId) {
        verifyHotelExists(hotelId);
        List<HotelAmenity> amenities = amenityRepository.findByHotelId(hotelId);
        return amenityMapper.toResponseList(amenities);
    }

    // ── Get amenities filtered by category ────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<HotelAmenityResponse> getAmenitiesByCategory(UUID hotelId, AmenityCategory category) {
        verifyHotelExists(hotelId);
        List<HotelAmenity> amenities = amenityRepository.findByHotelIdAndCategory(hotelId, category);
        return amenityMapper.toResponseList(amenities);
    }

    // ── Add amenity ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public HotelAmenityResponse addAmenity(UUID hotelId, UUID managerId, CreateAmenityRequest request) {
        // 1. Verify hotel exists
        verifyHotelExists(hotelId);

        // 2. Verify manager owns this hotel
        validateManagerOwnership(managerId, hotelId);

        // 3. Check for duplicate name (case-insensitive) within this hotel
        if (amenityRepository.existsByHotelIdAndNameIgnoreCase(hotelId, request.getName())) {
            throw new IllegalArgumentException(
                    "Amenity '" + request.getName() + "' already exists for this hotel"
            );
        }

        // 4. Build and save the amenity
        HotelAmenity amenity = HotelAmenity.builder()
                .hotelId(hotelId)
                .name(request.getName())
                .category(request.getCategory())
                .icon(request.getIcon())
                .build();

        amenity = amenityRepository.save(amenity);
        log.info("Added amenity '{}' ({}) to hotel {}", amenity.getName(), amenity.getCategory(), hotelId);

        return amenityMapper.toResponse(amenity);
    }

    // ── Remove amenity ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void removeAmenity(UUID hotelId, UUID amenityId, UUID managerId) {
        // 1. Fetch the amenity
        HotelAmenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelAmenity", amenityId));

        // 2. Verify it belongs to the specified hotel (prevents cross-hotel deletion)
        if (!amenity.getHotelId().equals(hotelId)) {
            throw new ResourceNotFoundException("HotelAmenity", amenityId);
        }

        // 3. Verify manager owns this hotel
        validateManagerOwnership(managerId, hotelId);

        // 4. Delete
        amenityRepository.delete(amenity);
        log.info("Removed amenity '{}' from hotel {}", amenity.getName(), hotelId);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void verifyHotelExists(UUID hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
    }

    /**
     * Validates that the authenticated manager owns the hotel.
     * Throws UnauthorizedException if not.
     */
    private void validateManagerOwnership(UUID managerId, UUID hotelId) {
        if (!hotelRepository.existsByIdAndManager_Id(hotelId, managerId)) {
            throw new UnauthorizedException(
                    "You do not own hotel " + hotelId
            );
        }
    }
}