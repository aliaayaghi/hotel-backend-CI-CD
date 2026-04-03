package com.HotelBook.HotelBooking.catalog.amenity;


import com.HotelBook.HotelBooking.catalog.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hotels/{hotelId}/amenities")
@RequiredArgsConstructor
@Tag(name = "Hotel Amenities", description = "Manage hotel-level amenities (pool, gym, spa, etc.)")
public class HotelAmenityController {

    private final HotelAmenityService amenityService;

    // ── GET /api/hotels/{hotelId}/amenities ────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "Get all hotel amenities",
            description = "Returns all amenities for a hotel. Optionally filter by category. Public endpoint — no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amenities returned"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<List<HotelAmenityResponse>> getAmenities(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Parameter(description = "Optional category filter: WELLNESS, DINING, TRANSPORT, CONNECTIVITY, PARKING, FAMILY, BUSINESS")
            @RequestParam(required = false) AmenityCategory category
    ) {
        if (category != null) {
            return ResponseEntity.ok(amenityService.getAmenitiesByCategory(hotelId, category));
        }
        return ResponseEntity.ok(amenityService.getAmenities(hotelId));
    }

    // ── POST /api/hotels/{hotelId}/amenities ───────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Add an amenity to a hotel",
            description = "Adds a new amenity. Only the hotel's manager can add amenities. Duplicate names (case-insensitive) within the same hotel are rejected."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Amenity added"),
            @ApiResponse(responseCode = "400", description = "Validation failed or duplicate name"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager or not the owner of this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<HotelAmenityResponse> addAmenity(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @AuthenticationPrincipal User currentUser,

            @Valid @RequestBody CreateAmenityRequest request
    ) {
        HotelAmenityResponse response = amenityService.addAmenity(hotelId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── DELETE /api/hotels/{hotelId}/amenities/{amenityId} ─────────────────────

    @DeleteMapping("/{amenityId}")
    @PreAuthorize("hasRole('HOTEL_MANAGER') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Remove an amenity",
            description = "Removes an amenity from a hotel. The hotel manager who owns the hotel, or an admin, can delete amenities."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Amenity removed"),
            @ApiResponse(responseCode = "403", description = "Not the owner of this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel or amenity not found")
    })
    public ResponseEntity<Void> removeAmenity(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Parameter(description = "Amenity UUID", required = true)
            @PathVariable UUID amenityId,

            @AuthenticationPrincipal User currentUser
    ) {
        amenityService.removeAmenity(hotelId, amenityId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}