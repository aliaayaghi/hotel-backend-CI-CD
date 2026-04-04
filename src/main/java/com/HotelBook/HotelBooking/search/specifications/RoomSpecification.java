package com.HotelBook.HotelBooking.search.specifications;


import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.room.BedType;
import com.HotelBook.HotelBooking.room.Room;
import com.HotelBook.HotelBooking.room.RoomType;
import com.HotelBook.HotelBooking.room.RoomView;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomSpecification {

    /**
     * Hotel must have at least one active room that can accommodate
     * the requested number of adults and children.
     */
    public static Specification<Hotel> canAccommodateGuests(int adults, int children) {
        return (root, query, cb) -> {
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<Room> room = sub.from(Room.class);

            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(room.get("hotelId"), root.get("id")));
            preds.add(cb.isTrue(room.get("isActive")));
            preds.add(cb.greaterThanOrEqualTo(room.get("maxAdults"), adults));

            if (children > 0) {
                preds.add(cb.greaterThanOrEqualTo(room.get("maxChildren"), children));
            }

            sub.select(room.get("id"))
                    .where(preds.toArray(new Predicate[0]));

            return cb.exists(sub);
        };
    }

    /**
     * Hotel must have at least one active room of one of the requested types.
     * Skips unknown type strings silently.
     */
    public static Specification<Hotel> hasRoomTypes(List<String> roomTypes) {
        return (root, query, cb) -> {
            List<RoomType> types = roomTypes.stream()
                    .map(t -> {
                        try { return RoomType.valueOf(t.toUpperCase().trim()); }
                        catch (IllegalArgumentException e) { return null; }
                    })
                    .filter(t -> t != null)
                    .toList();

            if (types.isEmpty()) return cb.conjunction();

            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<Room> room = sub.from(Room.class);
            sub.select(room.get("id"))
                    .where(cb.and(
                            cb.equal(room.get("hotelId"), root.get("id")),
                            cb.isTrue(room.get("isActive")),
                            room.get("type").in(types)
                    ));

            return cb.exists(sub);
        };
    }

    /**
     * Hotel must have at least one active room with one of the requested bed types.
     */
    public static Specification<Hotel> hasBedTypes(List<String> bedTypes) {
        return (root, query, cb) -> {
            List<BedType> beds = bedTypes.stream()
                    .map(b -> {
                        try { return BedType.valueOf(b.toUpperCase().trim()); }
                        catch (IllegalArgumentException e) { return null; }
                    })
                    .filter(b -> b != null)
                    .toList();

            if (beds.isEmpty()) return cb.conjunction();

            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<Room> room = sub.from(Room.class);
            sub.select(room.get("id"))
                    .where(cb.and(
                            cb.equal(room.get("hotelId"), root.get("id")),
                            cb.isTrue(room.get("isActive")),
                            room.get("bedType").in(beds)
                    ));

            return cb.exists(sub);
        };
    }

    /**
     * Hotel must have at least one active room with one of the requested views.
     */
    public static Specification<Hotel> hasViews(List<String> views) {
        return (root, query, cb) -> {
            List<RoomView> roomViews = views.stream()
                    .map(v -> {
                        try { return RoomView.valueOf(v.toUpperCase().trim()); }
                        catch (IllegalArgumentException e) { return null; }
                    })
                    .filter(v -> v != null)
                    .toList();

            if (roomViews.isEmpty()) return cb.conjunction();

            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<Room> room = sub.from(Room.class);
            sub.select(room.get("id"))
                    .where(cb.and(
                            cb.equal(room.get("hotelId"), root.get("id")),
                            cb.isTrue(room.get("isActive")),
                            room.get("view").in(roomViews)
                    ));

            return cb.exists(sub);
        };
    }

    /**
     * The cheapest active room in the hotel must be within the price range.
     */
    public static Specification<Hotel> hasPriceInRange(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            Subquery<BigDecimal> priceSub = query.subquery(BigDecimal.class);
            Root<Room> room = priceSub.from(Room.class);
            priceSub.select(cb.min(room.get("price")))
                    .where(cb.and(
                            cb.equal(room.get("hotelId"), root.get("id")),
                            cb.isTrue(room.get("isActive"))
                    ));

            if (min != null && max != null) {
                return cb.between(priceSub, min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(priceSub, min);
            } else {
                return cb.lessThanOrEqualTo(priceSub, max);
            }
        };
    }
}