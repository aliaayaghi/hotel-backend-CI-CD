package com.HotelBook.HotelBooking.catalog.photo;

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
@RequestMapping("/api/hotels/{hotelId}/photos")
@RequiredArgsConstructor
@Tag(name = "Hotel Photos", description = "Manage hotel gallery photos. GET endpoints are public. POST/DELETE/PATCH require HOTEL_MANAGER role.")
public class HotelPhotoController {

    private final HotelPhotoService photoService;

    // ── GET /api/hotels/{hotelId}/photos ──────────────────────────────────────
    // Public — no JWT required. Guests need to see hotel photos.

    @GetMapping
    @Operation(
            summary = "Get hotel photos",
            description = "Returns all photos for a hotel sorted by display_order ascending. Public endpoint — no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photos returned successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<List<HotelPhotoResponse>> getPhotos(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(photoService.getPhotos(hotelId));
    }

    // ── POST /api/hotels/{hotelId}/photos ─────────────────────────────────────
    // MANAGER only. Manager must own the hotel.

    @PostMapping
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Add a photo",
            description = "Adds a new photo to the hotel gallery. Set isCover=true to make this photo the thumbnail shown in listings. Any existing cover is automatically cleared."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Photo added successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — check URL format"),
            @ApiResponse(responseCode = "403", description = "Not a manager or you don't own this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<HotelPhotoResponse> addPhoto(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Valid @RequestBody CreatePhotoRequest request,

            @AuthenticationPrincipal User currentUser
    ) {
        HotelPhotoResponse response = photoService.addPhoto(hotelId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── DELETE /api/hotels/{hotelId}/photos/{photoId} ─────────────────────────
    // MANAGER only. Validates photo belongs to this hotel before deleting.

    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete a photo",
            description = "Permanently deletes a photo. The photo must belong to the specified hotel. If the deleted photo was the cover, no photo will be marked as cover until the manager sets a new one."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Photo deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not a manager or you don't own this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel or photo not found")
    })
    public ResponseEntity<Void> deletePhoto(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Parameter(description = "Photo UUID", required = true)
            @PathVariable UUID photoId,

            @AuthenticationPrincipal User currentUser
    ) {
        photoService.deletePhoto(hotelId, photoId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // ── PATCH /api/hotels/{hotelId}/photos/reorder ────────────────────────────
    // MANAGER only. Accepts the full ordered list of photo UUIDs.

    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Reorder photos",
            description = "Reassigns the display order for all hotel photos. The request body must include ALL photo UUIDs for the hotel in the desired order. The index of each UUID in the array becomes that photo's display_order value."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photos reordered successfully — returns updated list"),
            @ApiResponse(responseCode = "400", description = "Validation failed — photoIds must include all existing photos for this hotel"),
            @ApiResponse(responseCode = "403", description = "Not a manager or you don't own this hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found or a photo UUID is invalid")
    })
    public ResponseEntity<List<HotelPhotoResponse>> reorderPhotos(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Valid @RequestBody ReorderPhotosRequest request,

            @AuthenticationPrincipal User currentUser
    ) {
        List<HotelPhotoResponse> response = photoService.reorderPhotos(hotelId, currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }
}
