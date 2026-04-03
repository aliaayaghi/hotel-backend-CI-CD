package com.HotelBook.HotelBooking.catalog.policy;

import java.util.UUID;

public interface PetPolicyService {

    PetPolicyResponse getPolicy(UUID hotelId);

    /**
     * Create-or-update (upsert) the pet policy for the hotel.
     * Creates if no policy exists yet; updates if one already exists.
     * The caller uses the same endpoint for both cases — no separate POST vs PUT.
     */
    PetPolicyResponse upsertPolicy(UUID hotelId, CreatePetPolicyRequest request);
}
