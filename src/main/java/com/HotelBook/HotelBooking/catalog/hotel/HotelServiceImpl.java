package com.HotelBook.HotelBooking.catalog.hotel;


import com.HotelBook.HotelBooking.catalog.user.entity.HotelManager;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.catalog.user.repository.HotelManagerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final HotelMapper hotelMapper;

    // ── Public ────────────────────────────────────────────────────────────────

    /**
     * Public hotel search — only returns ACTIVE hotels.
     * Applies keyword, city, countryCode, type, and starRating filters progressively.
     * For Step 1 we use individual derived queries; for Step 2 replace with
     * a JPA Specification or QueryDSL predicate for arbitrary filter combinations.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HotelSummaryResponse> searchHotels(HotelSearchRequest filter, Pageable pageable) {
        Page<Hotel> hotels;

        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            hotels = hotelRepository.searchByKeyword(
                    HotelStatus.ACTIVE, filter.getKeyword().trim(), pageable);

        } else if (filter.getCity() != null && !filter.getCity().isBlank()) {
            hotels = hotelRepository.findAllByStatusAndCityContainingIgnoreCase(
                    HotelStatus.ACTIVE, filter.getCity().trim(), pageable);

        } else if (filter.getCountryCode() != null && !filter.getCountryCode().isBlank()) {
            hotels = hotelRepository.findAllByStatusAndCountryCode(
                    HotelStatus.ACTIVE, filter.getCountryCode().toUpperCase(), pageable);

        } else if (filter.getType() != null) {
            hotels = hotelRepository.findAllByStatusAndType(
                    HotelStatus.ACTIVE, filter.getType(), pageable);

        } else if (filter.getStarRating() != null) {
            hotels = hotelRepository.findAllByStatusAndStarRating(
                    HotelStatus.ACTIVE, filter.getStarRating(), pageable);

        } else {
            // No filters — return all active hotels
            hotels = hotelRepository.findAllByStatus(HotelStatus.ACTIVE, pageable);
        }

        return hotels.map(hotelMapper::toHotelSummaryResponse);
    }

    /**
     * Public hotel detail — only returns ACTIVE hotels.
     * Non-admin callers cannot see PENDING/REJECTED/SUSPENDED hotels.
     */
    @Override
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(UUID hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        if (hotel.getStatus() != HotelStatus.ACTIVE) {
            // Hide non-active hotels from the public — respond as if not found
            throw new HotelNotFoundException(hotelId);
        }

        return hotelMapper.toHotelResponse(hotel);
    }

    // ── Manager ───────────────────────────────────────────────────────────────

    /**
     * Create a new hotel for the authenticated manager.
     * Hotel starts as PENDING — admin must approve before it goes public.
     */
    @Override
    @Transactional
    public HotelResponse createHotel(UUID managerId, CreateHotelRequest request) {
        HotelManager manager = findManagerById(managerId);

        Hotel hotel = Hotel.builder()
                .manager(manager)
                .name(request.getName())
                .type(request.getType())
                .overview(request.getOverview())
                .starRating(request.getStarRating())
                .address(request.getAddress())
                .city(request.getCity())
                .countryCode(request.getCountryCode().toUpperCase())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .status(HotelStatus.PENDING)
                .build();

        hotel = hotelRepository.save(hotel);
        log.info("Manager {} created hotel: {} ({})", managerId, hotel.getName(), hotel.getId());

        return hotelMapper.toHotelResponse(hotel);
    }

    /**
     * Update hotel details.
     * Manager can only update hotels they own.
     * Patches only non-null fields.
     * If the hotel was REJECTED, updating it resets status back to PENDING
     * so the admin can review the corrected listing.
     */
    @Override
    @Transactional
    public HotelResponse updateHotel(UUID managerId, UUID hotelId, UpdateHotelRequest request) {
        Hotel hotel = findHotelOwnedByManager(managerId, hotelId);

        // Patch non-null fields only
        if (request.getName() != null)        hotel.setName(request.getName());
        if (request.getType() != null)        hotel.setType(request.getType());
        if (request.getOverview() != null)    hotel.setOverview(request.getOverview());
        if (request.getStarRating() != null)  hotel.setStarRating(request.getStarRating());
        if (request.getAddress() != null)     hotel.setAddress(request.getAddress());
        if (request.getCity() != null)        hotel.setCity(request.getCity());
        if (request.getCountryCode() != null) hotel.setCountryCode(request.getCountryCode().toUpperCase());
        if (request.getLatitude() != null)    hotel.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)   hotel.setLongitude(request.getLongitude());
        if (request.getPhone() != null)       hotel.setPhone(request.getPhone());
        if (request.getEmail() != null)       hotel.setEmail(request.getEmail());
        if (request.getWebsite() != null)     hotel.setWebsite(request.getWebsite());

        // If previously rejected and manager edits, re-submit for review
        if (hotel.getStatus() == HotelStatus.REJECTED) {
            hotel.setStatus(HotelStatus.PENDING);
            hotel.setRejectionReason(null);
            log.info("Hotel {} re-submitted for review after edit by manager {}", hotelId, managerId);
        }

        hotel = hotelRepository.save(hotel);
        log.info("Manager {} updated hotel: {}", managerId, hotelId);

        return hotelMapper.toHotelResponse(hotel);
    }

    /**
     * Soft-delete: manager sets their own hotel to SUSPENDED.
     * Hotel disappears from public search immediately.
     * Admin hard-delete is in AdminService.
     */
    @Override
    @Transactional
    public void suspendHotel(UUID managerId, UUID hotelId) {
        Hotel hotel = findHotelOwnedByManager(managerId, hotelId);
        hotel.setStatus(HotelStatus.SUSPENDED);
        hotelRepository.save(hotel);
        log.info("Manager {} suspended hotel: {}", managerId, hotelId);
    }

    /**
     * List all hotels owned by the authenticated manager.
     * Returns all statuses (PENDING, ACTIVE, REJECTED, SUSPENDED)
     * so the manager can see the full state of their portfolio.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> getMyHotels(UUID managerId, Pageable pageable) {
        return hotelRepository.findAllByManager_Id(managerId, pageable)
                .map(hotelMapper::toHotelResponse);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private HotelManager findManagerById(UUID managerId) {
        return hotelManagerRepository.findByUser_Id(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelManager", managerId));
    }

    /**
     * Fetch a hotel and verify the caller is its owner.
     * Throws HotelNotFoundException if not found (to avoid leaking existence to other managers).
     * Throws UnauthorizedHotelAccessException if found but owned by someone else.
     */
    private Hotel findHotelOwnedByManager(UUID managerId, UUID hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        if (!hotel.getManager().getId().equals(managerId)) {
            throw new UnauthorizedHotelAccessException(hotelId);
        }

        return hotel;
    }
}
