package com.HotelBook.HotelBooking.catalog.policy;


import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.catalog.hotel.HotelRepository;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInPolicyServiceImpl implements CheckInPolicyService {

    private final CheckInPolicyRepository checkInPolicyRepository;
    private final PolicyMapper policyMapper;
    private final HotelRepository hotelRepository;

    // ── Get ────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CheckInPolicyResponse getPolicy(UUID hotelId) {
        CheckInPolicy policy = checkInPolicyRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("CheckInPolicy for hotel", hotelId));
        return policyMapper.toCheckInResponse(policy);
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    /**
     * Creates the check-in policy.
     * Throws ConflictException if a policy already exists for this hotel —
     * the client should call updatePolicy() instead.
     */
    @Override
    @Transactional
    public CheckInPolicyResponse createPolicy(UUID hotelId, CreateCheckInPolicyRequest request) {
        if (checkInPolicyRepository.existsByHotelId(hotelId)) {
            // M3 will map ConflictException → 409 in GlobalExceptionHandler
            throw new ConflictException(
                    "Hotel " + hotelId + " already has a check-in policy. Use PUT to update it."
            );
        }

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        CheckInPolicy policy = CheckInPolicy.builder()
                .hotel(hotel)
                .earliestTime(request.getEarliestTime())
                .latestTime(request.getLatestTime())
                .earlyCheckIn(request.isEarlyCheckIn())
                .lateCheckOut(request.isLateCheckOut())
                .description(request.getDescription())
                .build();

        policy = checkInPolicyRepository.save(policy);
        log.info("Created check-in policy for hotel {}", hotelId);
        return policyMapper.toCheckInResponse(policy);
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    /**
     * Updates the existing check-in policy.
     * All fields are replaced (full update — not a patch).
     */
    @Override
    @Transactional
    public CheckInPolicyResponse updatePolicy(UUID hotelId, CreateCheckInPolicyRequest request) {
        CheckInPolicy policy = checkInPolicyRepository.findByHotelId(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("CheckInPolicy for hotel", hotelId));

        policy.setEarliestTime(request.getEarliestTime());
        policy.setLatestTime(request.getLatestTime());
        policy.setEarlyCheckIn(request.isEarlyCheckIn());
        policy.setLateCheckOut(request.isLateCheckOut());
        policy.setDescription(request.getDescription());

        policy = checkInPolicyRepository.save(policy);
        log.info("Updated check-in policy for hotel {}", hotelId);
        return policyMapper.toCheckInResponse(policy);
    }
}
