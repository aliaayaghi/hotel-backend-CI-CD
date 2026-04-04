package com.HotelBook.HotelBooking.search.specifications;


import com.HotelBook.HotelBooking.catalog.accessibility.HotelAccessibility;
import com.HotelBook.HotelBooking.catalog.accessibility.AccessibilityLevel;
import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class HotelAccessibilitySpecification {

    /**
     * Hotel must have at least one HotelAccessibility feature
     * with level = FULL or PARTIAL (not NONE).
     *
     * We use the AccessibilityLevel enum directly
     * since HotelAccessibility.level is an @Enumerated field.
     */
    public static Specification<Hotel> isWheelchairAccessible() {
        return (root, query, cb) -> {
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<HotelAccessibility> ha = sub.from(HotelAccessibility.class);

            sub.select(ha.get("id"))
                    .where(cb.and(
                            cb.equal(ha.get("hotelId"), root.get("id")),
                            ha.get("level").in(
                                    AccessibilityLevel.FULL,
                                    AccessibilityLevel.PARTIAL
                            )
                    ));

            return cb.exists(sub);
        };
    }
}