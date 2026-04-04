package com.HotelBook.HotelBooking.search.specifications;

import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.catalog.hotel.HotelStatus;
import com.HotelBook.HotelBooking.catalog.hotel.HotelType;
import com.HotelBook.HotelBooking.search.dto.SearchRequestDTO;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class HotelSearchSpecification {

    /**
     * Combines all individual specifications into one.
     *
     * Each specification is only added when its filter is actually present
     * in the request — null or empty filters are completely skipped,
     * so they never affect the query.
     *
     * Order of specifications:
     *   1. Always-on: status = ACTIVE
     *   2. Always-on: city match
     *   3. Always-on: availability check
     *   4. Always-on: guest capacity check
     *   5. Optional: stars, hotel type, price range
     *   6. Optional: room filters (type, bed, view)
     *   7. Optional: policy filters (pets, breakfast, cancellation, accessibility)
     *   8. Optional: amenity filters
     */
    public static Specification<Hotel> build(SearchRequestDTO dto) {

        // Start with always-required filters
        Specification<Hotel> spec = Specification
                .where(isActive())
                .and(inCity(dto.getCity()))
                .and(RoomAvailabilitySpecification.hasAvailableRooms(
                        dto.getCheckIn(), dto.getCheckOut(), dto.getRooms()))
                .and(RoomSpecification.canAccommodateGuests(
                        dto.getAdults(), dto.getChildren()));

        // ── HOTEL LEVEL ───────────────────────────────────────────────────────

        if (hasValues(dto.getStars())) {
            spec = spec.and(hasStars(dto.getStars()));
        }

        if (hasValues(dto.getHotelType())) {
            spec = spec.and(hasHotelTypes(dto.getHotelType()));
        }

        if (dto.getPriceMin() != null || dto.getPriceMax() != null) {
            spec = spec.and(RoomSpecification.hasPriceInRange(
                    dto.getPriceMin(), dto.getPriceMax()));
        }

        // ── ROOM LEVEL ────────────────────────────────────────────────────────

        if (hasValues(dto.getRoomType())) {
            spec = spec.and(RoomSpecification.hasRoomTypes(dto.getRoomType()));
        }

        if (hasValues(dto.getBedType())) {
            spec = spec.and(RoomSpecification.hasBedTypes(dto.getBedType()));
        }

        if (hasValues(dto.getView())) {
            spec = spec.and(RoomSpecification.hasViews(dto.getView()));
        }

        // ── POLICY LEVEL ──────────────────────────────────────────────────────

        if (Boolean.TRUE.equals(dto.getPetsAllowed())) {
            spec = spec.and(PetPolicySpecification.allowsPets());
        }

        if (Boolean.TRUE.equals(dto.getBreakfastIncluded())) {
            spec = spec.and(BreakfastPolicySpecification.hasBreakfastOffered());
        }

        if (Boolean.TRUE.equals(dto.getFreeCancellation())) {
            spec = spec.and(CancellationPolicySpecification.hasFreeCancellation());
        }

        if (Boolean.TRUE.equals(dto.getWheelchairAccessible())) {
            spec = spec.and(HotelAccessibilitySpecification.isWheelchairAccessible());
        }

        // ── AMENITY LEVEL ─────────────────────────────────────────────────────

        if (hasValues(dto.getHotelAmenities())) {
            spec = spec.and(HotelAmenitySpecification.hasAllAmenities(
                    dto.getHotelAmenities()));
        }

        if (hasValues(dto.getAmenityCategories())) {
            spec = spec.and(HotelAmenitySpecification.hasAllAmenityCategories(
                    dto.getAmenityCategories()));
        }

        return spec;
    }

    // ── BASE SPECIFICATIONS ───────────────────────────────────────────────────

    private static Specification<Hotel> isActive() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), HotelStatus.ACTIVE);
    }

    private static Specification<Hotel> inCity(String city) {
        return (root, query, cb) ->
                cb.equal(
                        cb.lower(root.get("city")),
                        city.toLowerCase().trim()
                );
    }

    private static Specification<Hotel> hasStars(List<Integer> stars) {
        return (root, query, cb) ->
                root.get("starRating").in(stars);
    }

    private static Specification<Hotel> hasHotelTypes(List<String> types) {
        List<HotelType> hotelTypes = types.stream()
                .map(t -> {
                    try { return HotelType.valueOf(t.toUpperCase().trim()); }
                    catch (IllegalArgumentException e) { return null; }
                })
                .filter(t -> t != null)
                .toList();

        if (hotelTypes.isEmpty()) return (root, query, cb) -> cb.conjunction();

        return (root, query, cb) -> root.get("type").in(hotelTypes);
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private static boolean hasValues(List<?> list) {
        return list != null && !list.isEmpty();
    }
}