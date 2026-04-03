package com.HotelBook.HotelBooking.catalog.policy;


import java.util.UUID;

public interface CheckInPolicyService {

    CheckInPolicyResponse getPolicy(UUID hotelId);

    /**
     * Create a new check-in policy for the hotel.
     * Throws ConflictException if a policy already exists — use updatePolicy() instead.
     */
    CheckInPolicyResponse createPolicy(UUID hotelId, CreateCheckInPolicyRequest request);

    /**
     * Update the existing check-in policy for the hotel.
     * Throws ResourceNotFoundException if no policy exists yet — use createPolicy() first.
     */
    CheckInPolicyResponse updatePolicy(UUID hotelId, CreateCheckInPolicyRequest request);
}
