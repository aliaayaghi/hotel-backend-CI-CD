package com.HotelBook.HotelBooking.search.specifications;


import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.room.Room;
import com.HotelBook.HotelBooking.roomavailability.RoomAvailability;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class RoomAvailabilitySpecification {

    /**
     * Hotel must have at least one active room that:
     *   1. Has quantity >= roomsNeeded
     *   2. Has ZERO dates in [checkIn, checkOut) where blockedCount >= quantity
     *
     * This means: no date in the range is fully blocked for that room.
     *
     * How it works:
     *   - Outer subquery: finds a Room belonging to this hotel
     *   - Inner subquery: counts how many dates in the range are fully blocked
     *   - If that count = 0, the room is fully available
     */
    public static Specification<Hotel> hasAvailableRooms(
            LocalDate checkIn, LocalDate checkOut, int roomsNeeded) {

        return (root, query, cb) -> {

            // Outer subquery — find a room that is available
            Subquery<UUID> roomSub = query.subquery(UUID.class);
            Root<Room> roomRoot = roomSub.from(Room.class);

            // Inner subquery — count fully blocked dates for this room
            Subquery<Long> blockedDateCount = query.subquery(Long.class);
            Root<RoomAvailability> avRoot = blockedDateCount.from(RoomAvailability.class);

            blockedDateCount
                    .select(cb.count(avRoot))
                    .where(cb.and(
                            // Availability record belongs to this room
                            cb.equal(avRoot.get("roomId"), roomRoot.get("id")),
                            // Date is within the requested range (checkIn inclusive, checkOut exclusive)
                            cb.greaterThanOrEqualTo(avRoot.get("date"), checkIn),
                            cb.lessThan(avRoot.get("date"), checkOut),
                            // This date is fully blocked — no rooms left
                            cb.greaterThanOrEqualTo(
                                    avRoot.get("blockedCount"),
                                    roomRoot.get("quantity")
                            )
                    ));

            // Room qualifies if: belongs to this hotel, is active,
            // has enough quantity, and has zero fully-blocked dates
            roomSub.select(roomRoot.get("id"))
                    .where(cb.and(
                            cb.equal(roomRoot.get("hotelId"), root.get("id")),
                            cb.isTrue(roomRoot.get("isActive")),
                            cb.greaterThanOrEqualTo(roomRoot.get("quantity"), roomsNeeded),
                            cb.equal(blockedDateCount, 0L)
                    ));

            return cb.exists(roomSub);
        };
    }
}