package com.HotelBook.HotelBooking.catalog.nearby;


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
@RequestMapping("/api/hotels/{hotelId}/nearby")
@RequiredArgsConstructor
@Tag(name = "Nearby Places", description = "Manage points of interest near the hotel (airports, beaches, malls, etc.)")
public class NearbyPlaceController {

    private final NearbyPlaceService nearbyPlaceService;

    // ── GET /api/hotels/{hotelId}/nearby ───────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "Get nearby places",
            description = "Returns all nearby places sorted by distance (closest first). Optionally filter by type. Public endpoint — no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nearby places returned"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<List<NearbyPlaceResponse>> getNearbyPlaces(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Parameter(description = "Optional type filter: AIRPORT, BEACH, MALL, RESTAURANT, LANDMARK, HOSPITAL")
            @RequestParam(required = false) NearbyPlaceType type
    ) {
        if (type != null) {
            return ResponseEntity.ok(nearbyPlaceService.getNearbyPlacesByType(hotelId, type));
        }
        return ResponseEntity.ok(nearbyPlaceService.getNearbyPlaces(hotelId));
    }

    // ── POST /api/hotels/{hotelId}/nearby ──────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Add a nearby place",
            description = "Adds a new point of interest near the hotel. Only the hotel's manager can add entries."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nearby place added"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager or not the owner of this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<NearbyPlaceResponse> addNearbyPlace(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @AuthenticationPrincipal User currentUser,

            @Valid @RequestBody CreateNearbyPlaceRequest request
    ) {
        NearbyPlaceResponse response = nearbyPlaceService.addNearbyPlace(hotelId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── DELETE /api/hotels/{hotelId}/nearby/{placeId} ──────────────────────────

    @DeleteMapping("/{placeId}")
    @PreAuthorize("hasRole('HOTEL_MANAGER') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Remove a nearby place",
            description = "Removes a nearby place entry. The owning hotel manager or an admin can delete."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Nearby place removed"),
            @ApiResponse(responseCode = "403", description = "Not the owner of this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel or nearby place not found")
    })
    public ResponseEntity<Void> removeNearbyPlace(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Parameter(description = "Nearby place UUID", required = true)
            @PathVariable UUID placeId,

            @AuthenticationPrincipal User currentUser
    ) {
        nearbyPlaceService.removeNearbyPlace(hotelId, placeId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
