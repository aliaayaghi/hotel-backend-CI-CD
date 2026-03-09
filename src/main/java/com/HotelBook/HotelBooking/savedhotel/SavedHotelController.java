package com.HotelBook.HotelBooking.savedhotel;




import com.HotelBook.HotelBooking.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/saved-hotels")
@RequiredArgsConstructor
public class SavedHotelController {

    private final SavedHotelService savedHotelService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedHotelResponseDTO>>> getSavedHotels(
            @RequestHeader("X-Customer-Id") UUID customerId) {

        List<SavedHotelResponseDTO> saved = savedHotelService.getSavedHotels(customerId);
        return ResponseEntity.ok(ApiResponse.success(
                saved.size() + " saved hotel(s) retrieved.", saved));
    }


    @PostMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<SavedHotelResponseDTO>> saveHotel(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID hotelId,
            @Valid @RequestBody(required = false) SavedHotelRequestDTO request) {

        // request may be null if no body was sent — service handles null gracefully
        SavedHotelResponseDTO saved = savedHotelService.saveHotel(customerId, hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hotel saved to wishlist.", saved));
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> unsaveHotel(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID hotelId) {

        savedHotelService.unsaveHotel(customerId, hotelId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{hotelId}/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkSavedStatus(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID hotelId) {

        boolean isSaved = savedHotelService.isSaved(customerId, hotelId);
        return ResponseEntity.ok(ApiResponse.success(
                "Status retrieved.", Map.of("saved", isSaved)));
    }

    @PatchMapping("/{hotelId}/notes")
    public ResponseEntity<ApiResponse<SavedHotelResponseDTO>> updateNotes(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID hotelId,
            @Valid @RequestBody SavedHotelRequestDTO request) {

        SavedHotelResponseDTO updated = savedHotelService.updateNotes(customerId, hotelId, request);
        return ResponseEntity.ok(ApiResponse.success("Notes updated successfully.", updated));
    }
}
