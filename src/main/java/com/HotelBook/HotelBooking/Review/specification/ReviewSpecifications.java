package com.HotelBook.HotelBooking.Review.specification;

import com.HotelBook.HotelBooking.Review.Entity.Review;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewSpecifications {

    private ReviewSpecifications() {}

    public static Specification<Review> hasHotelId(UUID hotelId) {
        return (root, query, cb) ->
                hotelId == null ? null : cb.equal(root.get("hotel").get("id"), hotelId);
    }

    public static Specification<Review> hasCustomerId(UUID customerId) {
        return (root, query, cb) ->
                customerId == null ? null : cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<Review> hasTravelType(Review.TravelType travelType) {
        return (root, query, cb) ->
                travelType == null ? null : cb.equal(root.get("travelType"), travelType);
    }

    public static Specification<Review> ratingGreaterThanOrEqual(Integer minRating) {
        return (root, query, cb) ->
                minRating == null ? null :
                        cb.greaterThanOrEqualTo(root.get("calculatedOverallRating"), minRating.doubleValue());
    }

    public static Specification<Review> createdBetween(LocalDateTime after, LocalDateTime before) {
        return (root, query, cb) -> {
            if (after == null && before == null) return null;
            if (after != null && before != null) return cb.between(root.get("createdAt"), after, before);
            if (after != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), after);
            return cb.lessThanOrEqualTo(root.get("createdAt"), before);
        };
    }

    public static Specification<Review> notHidden() {
        return (root, query, cb) -> cb.equal(root.get("isHidden"), false);
    }

    public static Specification<Review> onlyFlagged() {
        return (root, query, cb) -> cb.equal(root.get("isFlagged"), true);
    }
}