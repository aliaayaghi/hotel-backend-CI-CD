package com.HotelBook.HotelBooking.search.specifications;



import com.HotelBook.HotelBooking.cancellation.CancellationPolicy;
import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CancellationPolicySpecification {

    /**
     * Hotel must have at least one CancellationPolicy
     * where refundPercentage > 0 (meaning a refund IS offered).
     *
     * CancellationPolicy stores hotelId directly as a plain UUID column
     * so we query by hotelId — no join to Hotel needed.
     */
    public static Specification<Hotel> hasFreeCancellation() {
        return (root, query, cb) -> {
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<CancellationPolicy> cp = sub.from(CancellationPolicy.class);

            sub.select(cp.get("id"))
                    .where(cb.and(
                            cb.equal(cp.get("hotelId"), root.get("id")),
                            cb.greaterThan(cp.get("refundPercentage"), 0)
                    ));

            return cb.exists(sub);
        };
    }
}