package com.HotelBook.HotelBooking.search.specifications;

import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.catalog.policy.BreakfastPolicy;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class BreakfastPolicySpecification {

    /**
     * Hotel must have a BreakfastPolicy where breakfastOffered = true.
     * Uses a subquery so we don't join the policy table into the main query
     * (avoids row multiplication from multiple joins).
     */
    public static Specification<Hotel> hasBreakfastOffered() {
        return (root, query, cb) -> {
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<BreakfastPolicy> bp = sub.from(BreakfastPolicy.class);

            sub.select(bp.get("id"))
                    .where(cb.and(
                            cb.equal(bp.get("hotelId"), root.get("id")),
                            cb.isTrue(bp.get("breakfastOffered"))
                    ));

            return cb.exists(sub);
        };
    }
}