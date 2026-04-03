package com.HotelBook.HotelBooking.roomaccessibility;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/hotels/{hotelId}/rooms/{roomId}/accessibility")
@RequiredArgsConstructor
public class RoomAccessibilityController {

    private final RoomAccessibilityService accessibilityService;


    @GetMapping
    public ResponseEntity<List<RoomAccessibilityResponseDTO>> getAccessibility(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(accessibilityService.getAccessibilitiesByRoom(hotelId, roomId));
    }

    @PostMapping
    public ResponseEntity<RoomAccessibilityResponseDTO> addAccessibility(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @Valid @RequestBody RoomAccessibilityRequestDTO request) {
        RoomAccessibilityResponseDTO created = accessibilityService.addAccessibility(hotelId, roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/{accessId}")
    public ResponseEntity<RoomAccessibilityResponseDTO> updateAccessibility(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID accessId,
            @Valid @RequestBody RoomAccessibilityRequestDTO request) {
        return ResponseEntity.ok(
                accessibilityService.updateAccessibility(hotelId, roomId, accessId, request));
    }


    @DeleteMapping("/{accessId}")
    public ResponseEntity<Void> deleteAccessibility(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID accessId) {
        accessibilityService.deleteAccessibility(hotelId, roomId, accessId);
        return ResponseEntity.noContent().build();
    }
}
