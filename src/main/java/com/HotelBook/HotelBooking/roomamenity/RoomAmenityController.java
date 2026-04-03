package com.HotelBook.HotelBooking.roomamenity;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hotels/{hotelId}/rooms/{roomId}/amenities")
@RequiredArgsConstructor
public class RoomAmenityController {

    private final RoomAmenityService amenityService;


    @GetMapping
    public ResponseEntity<List<RoomAmenityResponseDTO>> getAmenities(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(amenityService.getAmenitiesByRoom(hotelId, roomId));
    }


    @PostMapping
    public ResponseEntity<RoomAmenityResponseDTO> addAmenity(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @Valid @RequestBody RoomAmenityRequestDTO request) {
        RoomAmenityResponseDTO amenity = amenityService.addAmenity(hotelId, roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(amenity);
    }


    @PutMapping("/{amenityId}")
    public ResponseEntity<RoomAmenityResponseDTO> updateAmenity(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID amenityId,
            @Valid @RequestBody RoomAmenityRequestDTO request) {
        return ResponseEntity.ok(amenityService.updateAmenity(hotelId, roomId, amenityId, request));
    }


    @DeleteMapping("/{amenityId}")
    public ResponseEntity<Void> deleteAmenity(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID amenityId) {
        amenityService.deleteAmenity(hotelId, roomId, amenityId);
        return ResponseEntity.noContent().build();
    }
}
