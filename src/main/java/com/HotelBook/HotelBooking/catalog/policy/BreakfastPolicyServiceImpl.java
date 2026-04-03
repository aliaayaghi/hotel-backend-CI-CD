package com.HotelBook.HotelBooking.catalog.policy;



import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BreakfastPolicyServiceImpl implements BreakfastPolicyService {

    private final BreakfastPolicyRepository breakfastPolicyRepository;
    private final PolicyMapper policyMapper;

    // ── Get ────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BreakfastPolicyResponse getPolicy(UUID hotelId) {
        BreakfastPolicy policy = breakfastPolicyRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("BreakfastPolicy for hotel", hotelId));
        return policyMapper.toBreakfastResponse(policy);
    }

    // ── Upsert ─────────────────────────────────────────────────────────────────

    /**
     * Upsert (create-or-update) the breakfast policy.
     * Same pattern as PetPolicyServiceImpl.
     *
     * Business rules enforced:
     *   1. If breakfastOffered=false → clear includedInPrice, pricePerPerson, type
     *   2. If includedInPrice=true  → clear pricePerPerson (it's free)
     */
    @Override
    @Transactional
    public BreakfastPolicyResponse upsertPolicy(UUID hotelId, CreateBreakfastPolicyRequest request) {
        BreakfastPolicy policy = breakfastPolicyRepository.findByHotelId(hotelId)
                .orElse(BreakfastPolicy.builder().hotelId(hotelId).build());

        policy.setBreakfastOffered(request.getBreakfastOffered());

        if (Boolean.FALSE.equals(request.getBreakfastOffered())) {
            // Rule 1: breakfast not offered → clear all related fields
            policy.setIncludedInPrice(false);
            policy.setPricePerPerson(null);
            policy.setType(null);
        } else {
            policy.setType(request.getType());
            policy.setIncludedInPrice(request.isIncludedInPrice());

            if (request.isIncludedInPrice()) {
                // Rule 2: included in price → no separate charge
                policy.setPricePerPerson(null);
            } else {
                policy.setPricePerPerson(request.getPricePerPerson());
            }
        }

        policy = breakfastPolicyRepository.save(policy);
        log.info("Upserted breakfast policy for hotel {} — offered={}, included={}",
                hotelId, policy.isBreakfastOffered(), policy.isIncludedInPrice());
        return policyMapper.toBreakfastResponse(policy);
    }
}