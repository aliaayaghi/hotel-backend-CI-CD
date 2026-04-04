package com.HotelBook.HotelBooking.search.service;

import com.HotelBook.HotelBooking.cancellation.CancellationPolicy;
import com.HotelBook.HotelBooking.cancellation.CancellationPolicyRepository;
import com.HotelBook.HotelBooking.catalog.amenity.HotelAmenity;
import com.HotelBook.HotelBooking.catalog.amenity.HotelAmenityRepository;
import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.catalog.hotel.HotelRepository;
import com.HotelBook.HotelBooking.catalog.hotel.HotelStatus;
import com.HotelBook.HotelBooking.catalog.policy.BreakfastPolicy;
import com.HotelBook.HotelBooking.catalog.policy.BreakfastPolicyRepository;
import com.HotelBook.HotelBooking.catalog.policy.PetPolicy;
import com.HotelBook.HotelBooking.catalog.policy.PetPolicyRepository;
import com.HotelBook.HotelBooking.common.pagination.PagedResponse;
import com.HotelBook.HotelBooking.room.Room;
import com.HotelBook.HotelBooking.room.RoomRepository;
import com.HotelBook.HotelBooking.roomavailability.RoomAvailability;
import com.HotelBook.HotelBooking.roomavailability.RoomAvailabilityRepository;
import com.HotelBook.HotelBooking.search.dto.SearchRequestDTO;
import com.HotelBook.HotelBooking.search.dto.SearchResponseDTO;
import com.HotelBook.HotelBooking.search.specifications.HotelSearchSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    // ── REPOSITORIES ──────────────────────────────────────────────────────────
    // We inject repositories directly instead of relying on Hotel entity
    // relationships that Member 1 hasn't added yet.
    // When Member 1 adds @OneToMany on Hotel, we can simplify this later.
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final HotelAmenityRepository hotelAmenityRepository;
    private final BreakfastPolicyRepository breakfastPolicyRepository;
    private final PetPolicyRepository petPolicyRepository;
    private final CancellationPolicyRepository cancellationPolicyRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SearchResponseDTO> search(SearchRequestDTO dto) {

        // 1. Validate children ages match children count
        if (dto.getChildren() > 0) {
            if (dto.getChildrenAges() == null ||
                    dto.getChildrenAges().size() != dto.getChildren()) {
                throw new IllegalArgumentException(
                        "childrenAges must contain exactly " + dto.getChildren() + " ages"
                );
            }
        }

        // 2. Validate date order
        if (!dto.getCheckOut().isAfter(dto.getCheckIn())) {
            throw new IllegalArgumentException(
                    "Check-out date must be after check-in date"
            );
        }

        // 3. Build sort and pageable
        Sort sort = buildSort(dto.getSortBy(), dto.getSortOrder());
        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), sort);

        // 4. Build specification and execute query
        Specification<Hotel> spec = HotelSearchSpecification.build(dto);
        Page<Hotel> page = hotelRepository.findAll(spec, pageable);

        // 5. Map to response DTOs — each hotel gets its own repo calls
        List<SearchResponseDTO> content = page.getContent().stream()
                .map(hotel -> toResponseDTO(hotel, dto.getCheckIn(), dto.getCheckOut()))
                .collect(Collectors.toList());

        log.info("Search city='{}' {}/{} → {} results",
                dto.getCity(), dto.getCheckIn(), dto.getCheckOut(),
                page.getTotalElements());

        // 6. Build PagedResponse using setters (avoids constructor mismatch)
        PagedResponse<SearchResponseDTO> response = new PagedResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> autocomplete(String query) {
        if (query == null || query.trim().length() < 2) return List.of();

        return hotelRepository
                .searchByKeyword(
                        HotelStatus.ACTIVE,
                        query.toLowerCase().trim(),
                        PageRequest.of(0, 10)
                )
                .stream()
                .map(Hotel::getName)
                .collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private SearchResponseDTO toResponseDTO(Hotel hotel,
                                            LocalDate checkIn,
                                            LocalDate checkOut) {
        UUID hotelId = hotel.getId();

        // Load rooms from repository — Hotel entity has no getRooms()
        List<Room> activeRooms = roomRepository
                .findByHotelIdAndIsActiveTrue(hotelId);

        // Top 5 amenity names for the search card
        List<String> topAmenities = hotelAmenityRepository
                .findByHotelId(hotelId)
                .stream()
                .map(HotelAmenity::getName)
                .limit(5)
                .collect(Collectors.toList());

        // Lowest room price
        BigDecimal lowestPrice = activeRooms.stream()
                .map(Room::getPrice)
                .min(BigDecimal::compareTo)       // unambiguous — Room.price is BigDecimal
                .orElse(BigDecimal.ZERO);

        // Count available rooms for the date range
        int availableRooms = activeRooms.stream()
                .mapToInt(room -> isRoomAvailable(room, checkIn, checkOut)
                        ? room.getQuantity() : 0)
                .sum();

        // Free cancellation — any room has a policy with refund > 0
        boolean freeCancellation = activeRooms.stream().anyMatch(room ->
                cancellationPolicyRepository
                        .findByRoomId(room.getId())
                        .stream()
                        .anyMatch(p -> p.getRefundPercentage() != null
                                && p.getRefundPercentage() > 0)
        );

        // Breakfast — load from BreakfastPolicyRepository
        boolean breakfastIncluded = breakfastPolicyRepository
                .findByHotelId(hotelId)
                .map(BreakfastPolicy::isBreakfastOffered)
                .orElse(false);

        // Pets — load from PetPolicyRepository
        boolean petsAllowed = petPolicyRepository
                .findByHotelId(hotelId)
                .map(PetPolicy::isPetsAllowed)
                .orElse(false);

        return SearchResponseDTO.builder()
                .id(hotelId)
                .name(hotel.getName())
                .starRating(hotel.getStarRating())
                .type(hotel.getType() != null ? hotel.getType().name() : null)
                .city(hotel.getCity())
                .countryCode(hotel.getCountryCode())
                .address(hotel.getAddress())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .lowestPrice(lowestPrice)
                .availableRooms(availableRooms)
                .topAmenities(topAmenities)
                .freeCancellationAvailable(freeCancellation)
                .breakfastIncluded(breakfastIncluded)
                .petsAllowed(petsAllowed)
                .build();
    }

    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        // Load availability records for this room in the date range
        List<RoomAvailability> blocked = roomAvailabilityRepository
                .findByRoomIdAndDateBetween(room.getId(), checkIn, checkOut);

        if (blocked.isEmpty()) return true; // No blocked dates → fully available

        // Room is unavailable if ANY date in the range is fully blocked
        return blocked.stream().noneMatch(a ->
                a.getBlockedCount() != null
                        && a.getBlockedCount() >= room.getQuantity()
        );
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        Sort.Direction dir = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return switch (sortBy == null ? "name" : sortBy.toLowerCase()) {
            case "stars" -> Sort.by(dir, "starRating");
            case "name"  -> Sort.by(dir, "name");
            // Price sort requires a subquery — not directly supported by JPA Sort
            // Using name as default; can be upgraded in Step 2
            default      -> Sort.by(dir, "name");
        };
    }
}