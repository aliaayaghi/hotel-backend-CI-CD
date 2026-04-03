package com.HotelBook.HotelBooking.catalog.policy;



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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * PolicyController
 *
 * Handles all three policy types under a single base path:
 *   /api/hotels/{hotelId}/policies
 *
 * Sub-paths:
 *   /checkin    — CheckInPolicy  (separate POST + PUT)
 *   /pets       — PetPolicy      (upsert: single POST)
 *   /breakfast  — BreakfastPolicy (upsert: single POST)
 *
 * GET endpoints are public (no auth).
 * POST/PUT endpoints require HOTEL_MANAGER role.
 */
@RestController
@RequestMapping("/api/hotels/{hotelId}/policies")
@RequiredArgsConstructor
@Tag(name = "Hotel Policies", description = "Check-in, pet, and breakfast policy management")
public class PolicyController {

    private final CheckInPolicyService checkInService;
    private final PetPolicyService petService;
    private final BreakfastPolicyService breakfastService;

    // ══════════════════════════════════════════════════════════════════════════
    // CHECK-IN POLICY
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/checkin")
    @Operation(
            summary = "Get check-in policy",
            description = "Returns the check-in policy for the hotel. Public endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy returned"),
            @ApiResponse(responseCode = "404", description = "No check-in policy found for this hotel")
    })
    public ResponseEntity<CheckInPolicyResponse> getCheckInPolicy(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(checkInService.getPolicy(hotelId));
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create check-in policy",
            description = "Creates the check-in policy for the hotel. Returns 409 if one already exists — use PUT to update."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Policy created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager"),
            @ApiResponse(responseCode = "409", description = "Check-in policy already exists — use PUT")
    })
    public ResponseEntity<CheckInPolicyResponse> createCheckInPolicy(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,
            @Valid @RequestBody CreateCheckInPolicyRequest request
    ) {
        CheckInPolicyResponse response = checkInService.createPolicy(hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/checkin")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update check-in policy",
            description = "Updates the existing check-in policy. All fields are replaced (full update). Returns 404 if no policy exists — use POST to create first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager"),
            @ApiResponse(responseCode = "404", description = "No check-in policy found — use POST to create first")
    })
    public ResponseEntity<CheckInPolicyResponse> updateCheckInPolicy(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,
            @Valid @RequestBody CreateCheckInPolicyRequest request
    ) {
        return ResponseEntity.ok(checkInService.updatePolicy(hotelId, request));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PET POLICY
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/pets")
    @Operation(
            summary = "Get pet policy",
            description = "Returns the pet policy for the hotel. Public endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy returned"),
            @ApiResponse(responseCode = "404", description = "No pet policy found for this hotel")
    })
    public ResponseEntity<PetPolicyResponse> getPetPolicy(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(petService.getPolicy(hotelId));
    }

    @PostMapping("/pets")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Set pet policy (upsert)",
            description = "Creates or updates the pet policy. If a policy already exists it will be updated — no 409 Conflict. If petsAllowed=false, petFee is automatically cleared."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager")
    })
    public ResponseEntity<PetPolicyResponse> upsertPetPolicy(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,
            @Valid @RequestBody CreatePetPolicyRequest request
    ) {
        return ResponseEntity.ok(petService.upsertPolicy(hotelId, request));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BREAKFAST POLICY
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/breakfast")
    @Operation(
            summary = "Get breakfast policy",
            description = "Returns the breakfast policy for the hotel. Public endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy returned"),
            @ApiResponse(responseCode = "404", description = "No breakfast policy found for this hotel")
    })
    public ResponseEntity<BreakfastPolicyResponse> getBreakfastPolicy(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(breakfastService.getPolicy(hotelId));
    }

    @PostMapping("/breakfast")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Set breakfast policy (upsert)",
            description = "Creates or updates the breakfast policy. Business rules: (1) if breakfastOffered=false, type and price fields are cleared; (2) if includedInPrice=true, pricePerPerson is cleared."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager")
    })
    public ResponseEntity<BreakfastPolicyResponse> upsertBreakfastPolicy(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,
            @Valid @RequestBody CreateBreakfastPolicyRequest request
    ) {
        return ResponseEntity.ok(breakfastService.upsertPolicy(hotelId, request));
    }
}
