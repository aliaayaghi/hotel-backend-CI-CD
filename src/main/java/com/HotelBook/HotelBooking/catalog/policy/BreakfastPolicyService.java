package com.HotelBook.HotelBooking.catalog.policy;


import java.util.UUID;

public interface BreakfastPolicyService {

    BreakfastPolicyResponse getPolicy(UUID hotelId);

    /**
     * Create-or-update (upsert) the breakfast policy for the hotel.
     * Same upsert pattern as PetPolicyService.
     */
    BreakfastPolicyResponse upsertPolicy(UUID hotelId, CreateBreakfastPolicyRequest request);
}