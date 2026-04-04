package com.HotelBook.HotelBooking.search.specifications;


import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.catalog.policy.PetPolicy;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class PetPolicySpecification {

    /**
     * Hotel must have a PetPolicy where petsAllowed = true.
     */
    public static Specification<Hotel> allowsPets() {
        return (root, query, cb) -> {
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<PetPolicy> pp = sub.from(PetPolicy.class);

            sub.select(pp.get("id"))
                    .where(cb.and(
                            cb.equal(pp.get("hotelId"), root.get("id")),
                            cb.isTrue(pp.get("petsAllowed"))
                    ));

            return cb.exists(sub);
        };
    }
}