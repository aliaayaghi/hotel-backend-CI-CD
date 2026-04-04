package com.HotelBook.HotelBooking.catalog.customer;


import com.HotelBook.HotelBooking.common.ApiResponse;
import com.HotelBook.HotelBooking.notification.NotificationResponse;
import com.HotelBook.HotelBooking.notification.NotificationService;
import com.HotelBook.HotelBooking.savedhotel.SavedHotelRequestDTO;
import com.HotelBook.HotelBooking.savedhotel.SavedHotelResponseDTO;
import com.HotelBook.HotelBooking.savedhotel.SavedHotelService;
import com.HotelBook.HotelBooking.catalog.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.Map;
import java.util.UUID;

/**
 * CustomerController
 *
 * Base path : /api/customers/me
 * Auth      : every endpoint requires CUSTOMER role — enforced at class level.
 *
 * ── Saved Hotels ──────────────────────────────────────────────────────────────
 * Delegates entirely to M2's SavedHotelService.
 * This controller owns the /api/customers/me/** route prefix and extracts
 * customerId from the JWT principal (via @AuthenticationPrincipal) so that
 * M2's service does NOT need to read the JWT itself.
 *
 * ── Bookings / Reviews ───────────────────────────────────────────────────────
 * Stubs — return empty lists in Step 1.
 * M2 fills /bookings, M3 fills /reviews when their services are ready.
 *
 * ── Notifications ────────────────────────────────────────────────────────────
 * Delegates to M1's NotificationService.
 */
@RestController
@RequestMapping("/api/customers/me")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer self-service: bookings, reviews, wishlist, notifications")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    // M2's service — owns all SavedHotel business logic
    private final SavedHotelService savedHotelService;

    // M1's notification service
    private final NotificationService notificationService;

    // ══════════════════════════════════════════════════════════════════════════
    // BOOKINGS  — stub (M2 fills this in Step 2)
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/bookings")
    @Operation(
            summary = "Get my bookings",
            description = "Step 1 stub — returns empty list. M2 will inject BookingService here."
    )
    public ResponseEntity<ApiResponse<List<Object>>> getBookings(
            @AuthenticationPrincipal User currentUser
    ) {
        // TODO M2: inject BookingService and call getBookingsByCustomerId(currentUser.getId())
        return ResponseEntity.ok(ApiResponse.success("0 booking(s) retrieved.", List.of()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // REVIEWS  — stub (M3 fills this in Step 2)
    // ══════════════════════════════════════════════════════════════════════════

    @GetMapping("/reviews")
    @Operation(
            summary = "Get my reviews",
            description = "Step 1 stub — returns empty list. M3 will inject ReviewService here."
    )
    public ResponseEntity<ApiResponse<List<Object>>> getReviews(
            @AuthenticationPrincipal User currentUser
    ) {
        // TODO M3: inject ReviewService and call getReviewsByCustomerId(currentUser.getId())
        return ResponseEntity.ok(ApiResponse.success("0 review(s) retrieved.", List.of()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SAVED HOTELS  — delegates to M2's SavedHotelService
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/customers/me/saved
     *
     * Returns all hotels the customer has saved, newest first.
     * Calls M2's SavedHotelService.getSavedHotels(customerId).
     */
    @GetMapping("/saved")
    @Operation(
            summary = "Get my saved hotels",
            description = "Returns all hotels in the customer's wishlist, sorted newest first."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Saved hotels returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<ApiResponse<List<SavedHotelResponseDTO>>> getSavedHotels(
            @AuthenticationPrincipal User currentUser
    ) {
        List<SavedHotelResponseDTO> saved = savedHotelService.getSavedHotels(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(
                saved.size() + " saved hotel(s) retrieved.", saved));
    }

    /**
     * POST /api/customers/me/saved/{hotelId}
     *
     * Saves a hotel to the customer's wishlist.
     * An optional JSON body {"notes": "..."} is forwarded to M2's service.
     * Returns 409 if the hotel is already saved.
     */
    @PostMapping("/saved/{hotelId}")
    @Operation(
            summary = "Save a hotel",
            description = "Adds a hotel to the customer's wishlist. Optionally include a notes body. Returns 409 if already saved."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Hotel saved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hotel not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Hotel already saved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<ApiResponse<SavedHotelResponseDTO>> saveHotel(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "UUID of the hotel to save", required = true)
            @PathVariable UUID hotelId,
            @Valid @RequestBody(required = false) SavedHotelRequestDTO request
    ) {
        // request may be null — M2's service handles null gracefully
        SavedHotelResponseDTO saved = savedHotelService.saveHotel(
                currentUser.getId(), hotelId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hotel saved to wishlist.", saved));
    }

    /**
     * DELETE /api/customers/me/saved/{hotelId}
     *
     * Removes a hotel from the wishlist.
     * Returns 404 if the hotel was not saved (M2's service throws ResourceNotFoundException).
     */
    @DeleteMapping("/saved/{hotelId}")
    @Operation(
            summary = "Unsave a hotel",
            description = "Removes a hotel from the customer's wishlist. Returns 404 if hotel was not saved."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Hotel removed from wishlist"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hotel was not in wishlist"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<Void> unsaveHotel(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "UUID of the hotel to unsave", required = true)
            @PathVariable UUID hotelId
    ) {
        savedHotelService.unsaveHotel(currentUser.getId(), hotelId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/customers/me/saved/{hotelId}/status
     *
     * Quick boolean check — "is this hotel in my wishlist?"
     * Useful for toggling a heart/bookmark icon on the hotel detail page.
     */
    @GetMapping("/saved/{hotelId}/status")
    @Operation(
            summary = "Check if hotel is saved",
            description = "Returns {\"saved\": true/false} for the given hotel."
    )
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkSavedStatus(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "UUID of the hotel to check", required = true)
            @PathVariable UUID hotelId
    ) {
        boolean isSaved = savedHotelService.isSaved(currentUser.getId(), hotelId);
        return ResponseEntity.ok(ApiResponse.success(
                "Status retrieved.", Map.of("saved", isSaved)));
    }

    /**
     * PATCH /api/customers/me/saved/{hotelId}/notes
     *
     * Updates the personal note on a saved hotel (e.g. "Great honeymoon option!").
     * Returns 404 if the hotel is not currently saved.
     */
    @PatchMapping("/saved/{hotelId}/notes")
    @Operation(
            summary = "Update notes on a saved hotel",
            description = "Updates the personal note for a hotel already in the wishlist. Returns 404 if not saved."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notes updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Notes exceed 500 characters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hotel not in wishlist"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<ApiResponse<SavedHotelResponseDTO>> updateNotes(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "UUID of the saved hotel", required = true)
            @PathVariable UUID hotelId,
            @Valid @RequestBody SavedHotelRequestDTO request
    ) {
        SavedHotelResponseDTO updated = savedHotelService.updateNotes(
                currentUser.getId(), hotelId, request);

        return ResponseEntity.ok(ApiResponse.success("Notes updated successfully.", updated));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS  — delegates to M1's NotificationService
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/customers/me/notifications
     *
     * Returns all notifications sent to this customer, newest first.
     */
    @GetMapping("/notifications")
    @Operation(
            summary = "Get my notifications",
            description = "Returns all notifications sent to the authenticated customer, sorted newest first."
    )
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal User currentUser
    ) {
        List<NotificationResponse> notifications =
                notificationService.getByRecipient(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(
                notifications.size() + " notification(s) retrieved.", notifications));
    }
}