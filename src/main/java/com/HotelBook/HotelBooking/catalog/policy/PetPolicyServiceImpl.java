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
public class PetPolicyServiceImpl implements PetPolicyService {

    private final PetPolicyRepository petPolicyRepository;
    private final PolicyMapper policyMapper;

    // ── Get ────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PetPolicyResponse getPolicy(UUID hotelId) {
        PetPolicy policy = petPolicyRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("PetPolicy for hotel", hotelId));
        return policyMapper.toPetResponse(policy);
    }

    // ── Upsert ─────────────────────────────────────────────────────────────────

    /**
     * Upsert (create-or-update) the pet policy.
     *
     * Pattern:
     *   findByHotelId().orElse(new PetPolicy()) → set fields → save
     *
     * This means the client ALWAYS calls POST /pets regardless of whether
     * a policy already exists. No 409 Conflict needed — simpler API surface.
     *
     * Business rule enforced here:
     *   If petsAllowed=false, petFee is set to null.
     */
    @Override
    @Transactional
    public PetPolicyResponse upsertPolicy(UUID hotelId, CreatePetPolicyRequest request) {
        // Load existing policy or create a new empty one
        PetPolicy policy = petPolicyRepository.findByHotelId(hotelId)
                .orElse(PetPolicy.builder().hotelId(hotelId).build());

        policy.setPetsAllowed(request.getPetsAllowed());

        // Clear fee if pets are not allowed — avoids confusing non-null fee
        if (Boolean.FALSE.equals(request.getPetsAllowed())) {
            policy.setPetFee(null);
        } else {
            policy.setPetFee(request.getPetFee());
        }

        policy = petPolicyRepository.save(policy);
        log.info("Upserted pet policy for hotel {} — petsAllowed={}", hotelId, policy.isPetsAllowed());
        return policyMapper.toPetResponse(policy);
    }
}
