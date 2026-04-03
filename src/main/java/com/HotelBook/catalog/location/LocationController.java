package com.HotelBook.catalog.location;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(name = "Location", description = "Hotel location management and proximity search")
public class LocationController {

    private final LocationService locationService;

    // ── POST /api/hotels/{hotelId}/location ────────────────────────────────────
    // Manager sets the location when creating/onboarding their hotel.
    // Admin can also call this if they manually create a hotel.

    @PostMapping("/hotels/{hotelId}/location")
    @PreAuthorize("hasRole('HOTEL_MANAGER') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create hotel location",
            description = "Sets the address and coordinates for a hotel. " +
                    "Each hotel can have only one location. " +
                    "Accessible by the hotel's manager or an admin."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Location created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Hotel not found"),
            @ApiResponse(responseCode = "409", description = "Location already exists for this hotel")
    })
    public ResponseEntity<LocationResponse> createLocation(
            @Parameter(description = "Hotel UUID") @PathVariable UUID hotelId,
            @Valid @RequestBody CreateLocationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locationService.createLocation(hotelId, request));
    }

    // ── GET /api/hotels/{hotelId}/location ─────────────────────────────────────
    // Public — anyone browsing the site can see where a hotel is located.

    @GetMapping("/hotels/{hotelId}/location")
    @Operation(
            summary = "Get hotel location",
            description = "Returns the full address, coordinates, and Google Maps Place ID for a hotel. Public endpoint — no token required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location returned"),
            @ApiResponse(responseCode = "404", description = "Hotel or location not found")
    })
    public ResponseEntity<LocationResponse> getLocation(
            @Parameter(description = "Hotel UUID") @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(locationService.getLocationByHotelId(hotelId));
    }

    // ── PATCH /api/hotels/{hotelId}/location ───────────────────────────────────
    // Only the hotel's manager or an admin can update location details.

    @PatchMapping("/hotels/{hotelId}/location")
    @PreAuthorize("hasRole('HOTEL_MANAGER') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update hotel location",
            description = "Partially updates the hotel's location. Only the fields included in the request body are changed (PATCH semantics)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not allowed"),
            @ApiResponse(responseCode = "404", description = "Hotel or location not found")
    })
    public ResponseEntity<LocationResponse> updateLocation(
            @Parameter(description = "Hotel UUID") @PathVariable UUID hotelId,
            @Valid @RequestBody UpdateLocationRequest request
    ) {
        return ResponseEntity.ok(locationService.updateLocation(hotelId, request));
    }

    // ── DELETE /api/hotels/{hotelId}/location ──────────────────────────────────
    // Admin-only — deleting a location without deleting the hotel is an edge case.

    @DeleteMapping("/hotels/{hotelId}/location")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete hotel location",
            description = "Removes the location record for a hotel. Admin only. " +
                    "Normally location deletion is handled by hotel deletion via cascade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Location deleted"),
            @ApiResponse(responseCode = "403", description = "Not an admin"),
            @ApiResponse(responseCode = "404", description = "Hotel or location not found")
    })
    public ResponseEntity<Void> deleteLocation(
            @Parameter(description = "Hotel UUID") @PathVariable UUID hotelId
    ) {
        locationService.deleteLocation(hotelId);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/hotels/nearby ─────────────────────────────────────────────────
    // Public — used by the map view on the frontend.
    // Returns hotels sorted by distance (closest first).
    //
    // Example: GET /api/hotels/nearby?lat=31.95&lng=35.91&radiusKm=10

    @GetMapping("/hotels/nearby")
    @Operation(
            summary = "Find nearby hotels",
            description = "Returns all active hotels within the specified radius (km) of the given coordinates, " +
                    "sorted by distance ascending. Uses a bounding-box pre-filter + Haversine formula for accuracy."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nearby hotels returned"),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or radius")
    })
    public ResponseEntity<List<NearbyHotelResponse>> findNearby(

            @Parameter(description = "Latitude of the search center (-90 to 90)", required = true)
            @RequestParam
            @DecimalMin(value = "-90.0",  message = "Latitude must be >= -90")
            @DecimalMax(value =  "90.0",  message = "Latitude must be <= 90")
            double lat,

            @Parameter(description = "Longitude of the search center (-180 to 180)", required = true)
            @RequestParam
            @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
            @DecimalMax(value =  "180.0", message = "Longitude must be <= 180")
            double lng,

            @Parameter(description = "Search radius in kilometres (1–100). Default: 10")
            @RequestParam(defaultValue = "10")
            @Positive(message = "Radius must be a positive number")
            @DecimalMax(value = "100.0", message = "Radius cannot exceed 100 km")
            double radiusKm
    ) {
        return ResponseEntity.ok(locationService.findNearby(lat, lng, radiusKm));
    }
}