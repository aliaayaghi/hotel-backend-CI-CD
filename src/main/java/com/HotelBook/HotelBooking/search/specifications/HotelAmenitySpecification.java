package com.HotelBook.HotelBooking.search.specifications;

import com.HotelBook.HotelBooking.catalog.amenity.AmenityCategory;
import com.HotelBook.HotelBooking.catalog.amenity.HotelAmenity;
import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class HotelAmenitySpecification {

    /**
     * Hotel must have ALL requested amenity names (not just one).
     *
     * We loop and add one EXISTS subquery per amenity name.
     * This is an AND condition — hotel needs pool AND gym AND wifi,
     * not pool OR gym OR wifi.
     */
    public static Specification<Hotel> hasAllAmenities(List<String> amenityNames) {
        return (root, query, cb) -> {
            Predicate[] predicates = amenityNames.stream()
                    .map(name -> {
                        Subquery<UUID> sub = query.subquery(UUID.class);
                        Root<HotelAmenity> ha = sub.from(HotelAmenity.class);
                        sub.select(ha.get("id"))
                                .where(cb.and(
                                        cb.equal(ha.get("hotelId"), root.get("id")),
                                        cb.equal(
                                                cb.lower(ha.get("name")),
                                                name.toLowerCase().trim()
                                        )
                                ));
                        return cb.exists(sub);
                    })
                    .toArray(Predicate[]::new);

            return cb.and(predicates);
        };
    }

    /**
     * Hotel must have at least one amenity in each of the requested categories.
     * Same AND logic — hotel needs WELLNESS AND CONNECTIVITY, not WELLNESS OR CONNECTIVITY.
     */
    public static Specification<Hotel> hasAllAmenityCategories(List<String> categoryNames) {
        return (root, query, cb) -> {
            Predicate[] predicates = categoryNames.stream()
                    .map(catName -> {
                        try {
                            AmenityCategory category = AmenityCategory.valueOf(
                                    catName.toUpperCase().trim()
                            );
                            Subquery<UUID> sub = query.subquery(UUID.class);
                            Root<HotelAmenity> ha = sub.from(HotelAmenity.class);
                            sub.select(ha.get("id"))
                                    .where(cb.and(
                                            cb.equal(ha.get("hotelId"), root.get("id")),
                                            cb.equal(ha.get("category"), category)
                                    ));
                            return cb.exists(sub);
                        } catch (IllegalArgumentException e) {
                            // Unknown category name — skip it, return always-true predicate
                            return cb.conjunction();
                        }
                    })
                    .toArray(Predicate[]::new);

            return cb.and(predicates);
        };
    }
}