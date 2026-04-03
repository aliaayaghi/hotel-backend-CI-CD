package com.HotelBook.HotelBooking.catalog.policy;


import org.springframework.stereotype.Component;

/**
 * PolicyMapper
 *
 * Converts all three policy entities → their response DTOs.
 * Kept in a single class because all three are tightly related (hotel policies)
 * and the mapping logic is trivial — field-by-field copy with no computation.
 *
 * Injected into:
 *   - CheckInPolicyServiceImpl
 *   - PetPolicyServiceImpl
 *   - BreakfastPolicyServiceImpl
 *   - HotelMapper (to build HotelDetailResponse)
 */
@Component
public class PolicyMapper {

    public CheckInPolicyResponse toCheckInResponse(CheckInPolicy policy) {
        if (policy == null) return null;
        return CheckInPolicyResponse.builder()
                .id(policy.getId())
                .earliestTime(policy.getEarliestTime())
                .latestTime(policy.getLatestTime())
                .earlyCheckIn(policy.isEarlyCheckIn())
                .lateCheckOut(policy.isLateCheckOut())
                .description(policy.getDescription())
                .build();
    }

    public PetPolicyResponse toPetResponse(PetPolicy policy) {
        if (policy == null) return null;
        return PetPolicyResponse.builder()
                .id(policy.getId())
                .petsAllowed(policy.isPetsAllowed())
                .petFee(policy.getPetFee())
                .build();
    }

    public BreakfastPolicyResponse toBreakfastResponse(BreakfastPolicy policy) {
        if (policy == null) return null;
        return BreakfastPolicyResponse.builder()
                .id(policy.getId())
                .breakfastOffered(policy.isBreakfastOffered())
                .includedInPrice(policy.isIncludedInPrice())
                .pricePerPerson(policy.getPricePerPerson())
                .type(policy.getType())
                .build();
    }
}
