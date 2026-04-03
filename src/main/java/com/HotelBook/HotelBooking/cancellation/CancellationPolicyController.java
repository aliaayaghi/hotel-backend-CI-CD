package com.HotelBook.HotelBooking.cancellation;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CancellationPolicyController {

    private final CancellationPolicyService policyService;

    @GetMapping("/api/hotels/{hotelId}/rooms/{roomId}/cancellation-policies")
    public ResponseEntity<List<CancellationPolicyResponseDTO>> getRoomPolicies(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(policyService.getPoliciesForRoom(hotelId, roomId));
    }


    @PostMapping("/api/hotels/{hotelId}/rooms/{roomId}/cancellation-policies")
    public ResponseEntity<CancellationPolicyResponseDTO> createRoomPolicy(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @Valid @RequestBody CancellationPolicyRequestDTO request) {

        CancellationPolicyResponseDTO created = policyService.createRoomPolicy(hotelId, roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/api/hotels/{hotelId}/rooms/{roomId}/cancellation-policies/{policyId}")
    public ResponseEntity<CancellationPolicyResponseDTO> updateRoomPolicy(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID policyId,
            @Valid @RequestBody CancellationPolicyRequestDTO request) {

        return ResponseEntity.ok(policyService.updateRoomPolicy(hotelId, roomId, policyId, request));
    }


    @DeleteMapping("/api/hotels/{hotelId}/rooms/{roomId}/cancellation-policies/{policyId}")
    public ResponseEntity<Void> deleteRoomPolicy(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID policyId) {

        policyService.deleteRoomPolicy(roomId, policyId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/api/hotels/{hotelId}/cancellation-policies")
    public ResponseEntity<List<CancellationPolicyResponseDTO>> getHotelPolicies(
            @PathVariable UUID hotelId) {
        return ResponseEntity.ok(policyService.getHotelWidePolicies(hotelId));
    }

    @PostMapping("/api/hotels/{hotelId}/cancellation-policies")
    public ResponseEntity<CancellationPolicyResponseDTO> createHotelPolicy(
            @PathVariable UUID hotelId,
            @Valid @RequestBody CancellationPolicyRequestDTO request) {

        CancellationPolicyResponseDTO created = policyService.createHotelWidePolicy(hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/api/hotels/{hotelId}/cancellation-policies/{policyId}")
    public ResponseEntity<CancellationPolicyResponseDTO> updateHotelPolicy(
            @PathVariable UUID hotelId,
            @PathVariable UUID policyId,
            @Valid @RequestBody CancellationPolicyRequestDTO request) {

        return ResponseEntity.ok(policyService.updateHotelWidePolicy(hotelId, policyId, request));
    }

    @DeleteMapping("/api/hotels/{hotelId}/cancellation-policies/{policyId}")
    public ResponseEntity<Void> deleteHotelPolicy(
            @PathVariable UUID hotelId,
            @PathVariable UUID policyId) {

        policyService.deleteHotelWidePolicy(hotelId, policyId);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ADMIN / OVERVIEW ENDPOINTS
    // ══════════════════════════════════════════════════════════════════════════


    @GetMapping("/api/hotels/{hotelId}/cancellation-policies/all")
    public ResponseEntity<List<CancellationPolicyResponseDTO>> getAllPolicies(
            @PathVariable UUID hotelId) {
        return ResponseEntity.ok(policyService.getAllPoliciesForHotel(hotelId));
    }

    @GetMapping("/api/cancellation-policies/{policyId}")
    public ResponseEntity<CancellationPolicyResponseDTO> getPolicyById(
            @PathVariable UUID policyId) {
        return ResponseEntity.ok(policyService.getPolicyById(policyId));
    }
}
