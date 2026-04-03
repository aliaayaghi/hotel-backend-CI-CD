package com.HotelBook.HotelBooking.cancellation;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationPolicyService {

    /** Maximum number of cancellation tiers a single room can have. */
    private static final int MAX_TIERS_PER_ROOM = 5;

    private final CancellationPolicyRepository policyRepository;


    @Transactional(readOnly = true)
    public List<CancellationPolicyResponseDTO> getPoliciesForRoom(UUID hotelId, UUID roomId) {

        // Step 1: Look for room-specific tiers
        List<CancellationPolicy> policies = policyRepository.findByRoomId(roomId);

        // Step 2: If no room-specific tiers → fall back to hotel-wide defaults
        if (policies.isEmpty()) {
            policies = policyRepository.findByHotelIdAndRoomIdIsNull(hotelId);
            log.debug("No room-specific policies for room {} — using hotel-wide defaults", roomId);
        }

        return policies.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<CancellationPolicyResponseDTO> getHotelWidePolicies(UUID hotelId) {
        return policyRepository.findByHotelIdAndRoomIdIsNull(hotelId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CancellationPolicyResponseDTO> getAllPoliciesForHotel(UUID hotelId) {
        return policyRepository.findByHotelId(hotelId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public CancellationPolicyResponseDTO getPolicyById(UUID policyId) {
        CancellationPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException(
                        "Cancellation policy not found with id: " + policyId));
        return toResponseDTO(policy);
    }


    @Transactional(readOnly = true)
    public CancellationPolicy getPolicyEntityById(UUID policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException(
                        "Cancellation policy not found with id: " + policyId));
    }



    @Transactional
    public CancellationPolicyResponseDTO createRoomPolicy(UUID hotelId, UUID roomId,
                                                          CancellationPolicyRequestDTO request) {
        // Rule 1: enforce tier limit
        if (policyRepository.countByRoomId(roomId) >= MAX_TIERS_PER_ROOM) {
            throw new RuntimeException(
                    "Room already has the maximum of " + MAX_TIERS_PER_ROOM + " cancellation tiers.");
        }

        // Rule 3: non-refundable consistency check
        validateConsistency(request);

        // Rule 2: if this is the new default, clear any existing default for this room
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            policyRepository.clearDefaultForRoom(roomId);
        }

        CancellationPolicy policy = CancellationPolicy.builder()
                .hotelId(hotelId)
                .roomId(roomId)
                .tierName(request.getTierName())
                .deadlineHours(request.getDeadlineHours())
                .refundPercentage(request.getRefundPercentage())
                .priceMultiplier(request.getPriceMultiplier())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .description(request.getDescription())
                .build();

        CancellationPolicy saved = policyRepository.save(policy);
        log.info("Cancellation tier '{}' created for room {}", request.getTierName(), roomId);
        return toResponseDTO(saved);
    }

    @Transactional
    public CancellationPolicyResponseDTO createHotelWidePolicy(UUID hotelId,
                                                               CancellationPolicyRequestDTO request) {
        validateConsistency(request);

        // If this is the new hotel-wide default, clear existing hotel-wide default
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            policyRepository.clearDefaultForHotel(hotelId);
        }

        CancellationPolicy policy = CancellationPolicy.builder()
                .hotelId(hotelId)
                .roomId(null)          // null = hotel-wide default
                .tierName(request.getTierName())
                .deadlineHours(request.getDeadlineHours())
                .refundPercentage(request.getRefundPercentage())
                .priceMultiplier(request.getPriceMultiplier())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .description(request.getDescription())
                .build();

        CancellationPolicy saved = policyRepository.save(policy);
        log.info("Hotel-wide cancellation tier '{}' created for hotel {}", request.getTierName(), hotelId);
        return toResponseDTO(saved);
    }


    @Transactional
    public CancellationPolicyResponseDTO updateRoomPolicy(UUID hotelId, UUID roomId,
                                                          UUID policyId,
                                                          CancellationPolicyRequestDTO request) {

        CancellationPolicy policy = policyRepository.findByIdAndRoomId(policyId, roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Cancellation policy not found with id: " + policyId + " for room: " + roomId));

        applyUpdates(policy, request);

        // If setting as new default, clear the old one first
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(policy.getIsDefault())) {
            policyRepository.clearDefaultForRoom(roomId);
            policy.setIsDefault(true);
        }

        CancellationPolicy saved = policyRepository.save(policy);
        log.info("Cancellation tier {} updated for room {}", policyId, roomId);
        return toResponseDTO(saved);
    }


    @Transactional
    public CancellationPolicyResponseDTO updateHotelWidePolicy(UUID hotelId, UUID policyId,
                                                               CancellationPolicyRequestDTO request) {

        CancellationPolicy policy = policyRepository.findByIdAndHotelIdAndRoomIdIsNull(policyId, hotelId)
                .orElseThrow(() -> new RuntimeException(
                        "Hotel-wide policy not found with id: " + policyId + " for hotel: " + hotelId));

        applyUpdates(policy, request);

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(policy.getIsDefault())) {
            policyRepository.clearDefaultForHotel(hotelId);
            policy.setIsDefault(true);
        }

        CancellationPolicy saved = policyRepository.save(policy);
        log.info("Hotel-wide policy {} updated for hotel {}", policyId, hotelId);
        return toResponseDTO(saved);
    }


    @Transactional
    public void deleteRoomPolicy(UUID roomId, UUID policyId) {
        CancellationPolicy policy = policyRepository.findByIdAndRoomId(policyId, roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Cancellation policy not found with id: " + policyId + " for room: " + roomId));

        policyRepository.delete(policy);
        log.info("Cancellation tier {} deleted from room {}", policyId, roomId);
    }


    @Transactional
    public void deleteHotelWidePolicy(UUID hotelId, UUID policyId) {
        CancellationPolicy policy = policyRepository.findByIdAndHotelIdAndRoomIdIsNull(policyId, hotelId)
                .orElseThrow(() -> new RuntimeException(
                        "Hotel-wide policy not found with id: " + policyId + " for hotel: " + hotelId));

        policyRepository.delete(policy);
        log.info("Hotel-wide policy {} deleted from hotel {}", policyId, hotelId);
    }


    public BigDecimal calculateRefund(UUID policyId, LocalDateTime cancelledAt,
                                      LocalDateTime checkInDateTime, BigDecimal amountPaid) {

        CancellationPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException(
                        "Cancellation policy not found with id: " + policyId));

        BigDecimal refund = policy.calculateRefund(cancelledAt, checkInDateTime, amountPaid);

        log.info("Refund calculated for policy {}: {} (paid={}, cancelled={}, checkIn={})",
                policyId, refund, amountPaid, cancelledAt, checkInDateTime);

        return refund;
    }


    private void validateConsistency(CancellationPolicyRequestDTO request) {
        if (request.getDeadlineHours() == 0 && request.getRefundPercentage() > 0) {
            throw new RuntimeException(
                    "Non-refundable tier (deadlineHours=0) cannot have a refundPercentage > 0. " +
                            "Set refundPercentage to 0 for a non-refundable tier.");
        }

        if (request.getRefundPercentage() > 0 && request.getDeadlineHours() == 0) {
            throw new RuntimeException(
                    "A refundPercentage > 0 requires a deadlineHours > 0 " +
                            "(customers need a window to cancel and get their refund).");
        }
    }

    /** Applies partial updates from request to entity (only non-null fields). */
    private void applyUpdates(CancellationPolicy policy, CancellationPolicyRequestDTO request) {
        if (request.getTierName() != null)        policy.setTierName(request.getTierName());
        if (request.getDeadlineHours() != null)   policy.setDeadlineHours(request.getDeadlineHours());
        if (request.getRefundPercentage() != null) policy.setRefundPercentage(request.getRefundPercentage());
        if (request.getPriceMultiplier() != null) policy.setPriceMultiplier(request.getPriceMultiplier());
        if (request.getDescription() != null)     policy.setDescription(request.getDescription());
    }


    private CancellationPolicyResponseDTO toResponseDTO(CancellationPolicy policy) {
        CancellationPolicyResponseDTO dto = new CancellationPolicyResponseDTO();
        dto.setId(policy.getId());
        dto.setHotelId(policy.getHotelId());
        dto.setRoomId(policy.getRoomId());
        dto.setTierName(policy.getTierName());
        dto.setDeadlineHours(policy.getDeadlineHours());
        dto.setRefundPercentage(policy.getRefundPercentage());
        dto.setPriceMultiplier(policy.getPriceMultiplier());
        dto.setIsDefault(policy.getIsDefault());
        dto.setDescription(policy.getDescription());
        return dto;
    }
}
