package com.HotelBook.HotelBooking.catalog.hotel;


import com.HotelBook.HotelBooking.catalog.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Public hotel browsing and manager hotel management")
public class HotelController {

    private final HotelService hotelService;

    // ══════════════════════════════════════════════════════════════════════════
    // PUBLIC — No authentication required
    // (permitted in SecurityConfig: GET /api/hotels/**)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/hotels
     * Public search endpoint. Supports keyword, city, countryCode, type, starRating.
     * Only ACTIVE hotels are returned.
     *
     * Examples:
     *   GET /api/hotels                          → all active hotels
     *   GET /api/hotels?keyword=ramallah         → free-text search
     *   GET /api/hotels?city=Bethlehem           → by city
     *   GET /api/hotels?countryCode=PS           → by country
     *   GET /api/hotels?type=RESORT&starRating=5 → resort + 5-star (uses type filter)
     *   GET /api/hotels?page=1&size=10&sort=name,asc
     */
    @GetMapping
    @Operation(
            summary = "Search hotels",
            description = "Returns paginated ACTIVE hotels. Supports keyword, city, countryCode, type, and starRating filters. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Hotels returned successfully")
    public ResponseEntity<Page<HotelSummaryResponse>> searchHotels(
            @ModelAttribute HotelSearchRequest filter,

            @PageableDefault(size = 20, sort = "name")
            Pageable pageable
    ) {
        return ResponseEntity.ok(hotelService.searchHotels(filter, pageable));
    }

    /**
     * GET /api/hotels/{id}
     * Public hotel detail. Returns 404 for non-ACTIVE hotels.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get hotel by ID",
            description = "Returns full hotel details. Only ACTIVE hotels are visible publicly. No authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel found"),
            @ApiResponse(responseCode = "404", description = "Hotel not found or not active")
    })
    public ResponseEntity<HotelResponse> getHotelById(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MANAGER — Requires HOTEL_MANAGER role
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/hotels
     * Create a new hotel. Starts as PENDING — must be approved by admin.
     */
    @PostMapping
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create a hotel",
            description = "Manager creates a new hotel. It starts as PENDING and becomes visible only after admin approval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hotel created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager")
    })
    public ResponseEntity<HotelResponse> createHotel(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateHotelRequest request
    ) {
        HotelResponse response = hotelService.createHotel(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/hotels/{id}
     * Update hotel details. Manager can only update their own hotels.
     * If the hotel was REJECTED, editing it resets status to PENDING for re-review.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update a hotel",
            description = "Manager updates their own hotel. Partial update — only send fields you want to change. Updating a REJECTED hotel resets it to PENDING."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not the owner of this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<HotelResponse> updateHotel(
            @AuthenticationPrincipal User currentUser,

            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID id,

            @Valid @RequestBody UpdateHotelRequest request
    ) {
        HotelResponse response = hotelService.updateHotel(currentUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/hotels/{id}
     * Soft-delete: sets hotel status to SUSPENDED.
     * The hotel is hidden from public search but NOT removed from the database.
     * Admin hard-delete is at DELETE /api/admin/hotels/{id}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Suspend (soft-delete) a hotel",
            description = "Manager soft-deletes their hotel — sets status to SUSPENDED. Reversible. Use the admin endpoint for permanent deletion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hotel suspended successfully"),
            @ApiResponse(responseCode = "403", description = "Not the owner of this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<Void> suspendHotel(
            @AuthenticationPrincipal User currentUser,

            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID id
    ) {
        hotelService.suspendHotel(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/hotels/my
     * List all hotels owned by the authenticated manager.
     * Returns all statuses (PENDING, ACTIVE, REJECTED, SUSPENDED).
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my hotels",
            description = "Returns all hotels owned by the authenticated manager, across all statuses (PENDING, ACTIVE, REJECTED, SUSPENDED)."
    )
    @ApiResponse(responseCode = "200", description = "Hotels returned successfully")
    public ResponseEntity<Page<HotelResponse>> getMyHotels(
            @AuthenticationPrincipal User currentUser,

            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable
    ) {
        return ResponseEntity.ok(hotelService.getMyHotels(currentUser.getId(), pageable));
    }
}

