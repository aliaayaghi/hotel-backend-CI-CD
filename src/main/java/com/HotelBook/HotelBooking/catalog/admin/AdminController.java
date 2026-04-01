package com.HotelBook.HotelBooking.catalog.admin;

import com.HotelBook.HotelBooking.catalog.hotel.HotelResponse;
import com.HotelBook.HotelBooking.catalog.hotel.HotelStatus;
import com.HotelBook.HotelBooking.catalog.user.dto.response.UserResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")          // every endpoint in this controller requires ADMIN
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only hotel and user management endpoints")
@SecurityRequirement(name = "bearerAuth")  // tells Swagger to send JWT on these endpoints
public class AdminController {

    private final AdminService adminService;

    // ══════════════════════════════════════════════════════════════════════════
    // HOTEL MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/admin/hotels
     * List all hotels with optional status filter and pagination.
     *
     * Query params:
     *   status  — optional, one of: PENDING | ACTIVE | REJECTED | SUSPENDED
     *   page    — default 0
     *   size    — default 20
     *   sort    — e.g. createdAt,desc
     *
     * Examples:
     *   GET /api/admin/hotels               → all hotels, all statuses
     *   GET /api/admin/hotels?status=PENDING → only pending hotels
     */
    @GetMapping("/hotels")
    @Operation(
            summary = "List all hotels",
            description = "Returns a paginated list of hotels. Optionally filter by status (PENDING, ACTIVE, REJECTED, SUSPENDED). Admins see all statuses, unlike the public endpoint which only shows ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotels returned successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public ResponseEntity<Page<HotelResponse>> getAllHotels(
            @Parameter(description = "Filter by hotel status. Omit to get all hotels.")
            @RequestParam(required = false) HotelStatus status,

            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAllHotels(status, pageable));
    }

    /**
     * PATCH /api/admin/hotels/{id}/approve
     * Approve a hotel — changes status from PENDING to ACTIVE.
     * The hotel will immediately appear in public search results.
     */
    @PatchMapping("/hotels/{id}/approve")
    @Operation(
            summary = "Approve a hotel",
            description = "Changes hotel status from PENDING to ACTIVE. The hotel becomes visible in public listings immediately."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel approved successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel not found"),
            @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public ResponseEntity<HotelResponse> approveHotel(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(adminService.approveHotel(id));
    }

    /**
     * PATCH /api/admin/hotels/{id}/reject
     * Reject a hotel with a mandatory reason.
     * The hotel status becomes REJECTED and is hidden from public search.
     */
    @PatchMapping("/hotels/{id}/reject")
    @Operation(
            summary = "Reject a hotel",
            description = "Changes hotel status to REJECTED. Requires a reason that will be communicated to the hotel manager."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Reason is blank or too short"),
            @ApiResponse(responseCode = "404", description = "Hotel not found"),
            @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public ResponseEntity<HotelResponse> rejectHotel(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID id,

            @Valid @RequestBody RejectHotelRequest request
    ) {
        return ResponseEntity.ok(adminService.rejectHotel(id, request.getReason()));
    }

    /**
     * DELETE /api/admin/hotels/{id}
     * Hard-delete a hotel and all associated records (photos, amenities, rooms, etc.).
     * This is irreversible. Managers use soft-delete (status = SUSPENDED) instead.
     */
    @DeleteMapping("/hotels/{id}")
    @Operation(
            summary = "Hard-delete a hotel",
            description = "Permanently deletes a hotel and all its child records. Irreversible. Use reject or suspend for reversible actions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hotel deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel not found"),
            @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public ResponseEntity<Void> deleteHotel(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID id
    ) {
        adminService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // USER MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/admin/users
     * List all users with pagination.
     *
     * Query params:
     *   page — default 0
     *   size — default 20
     *   sort — e.g. createdAt,desc
     */
    @GetMapping("/users")
    @Operation(
            summary = "List all users",
            description = "Returns a paginated list of all users regardless of role or active status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users returned successfully"),
            @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    /**
     * PATCH /api/admin/users/{id}/suspend
     * Suspend a user — sets isActive = false.
     * The user cannot log in and any existing JWT tokens will be rejected
     * on the next request because Spring Security checks isAccountNonLocked().
     */
    @PatchMapping("/users/{id}/suspend")
    @Operation(
            summary = "Suspend a user",
            description = "Sets isActive=false. The user is immediately locked out. Existing JWT tokens are invalidated on next request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User suspended successfully"),
            @ApiResponse(responseCode = "400", description = "Reason is blank, or tried to suspend an admin"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public ResponseEntity<UserResponse> suspendUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id,

            @Valid @RequestBody SuspendUserRequest request
    ) {
        return ResponseEntity.ok(adminService.suspendUser(id, request.getReason()));
    }

    /**
     * PATCH /api/admin/users/{id}/unsuspend
     * Restore a suspended user — sets isActive = true.
     * The user can log in again immediately.
     */
    @PatchMapping("/users/{id}/unsuspend")
    @Operation(
            summary = "Unsuspend a user",
            description = "Restores a previously suspended user. Sets isActive=true — the user can log in again immediately."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User unsuspended successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public ResponseEntity<UserResponse> unsuspendUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(adminService.unsuspendUser(id));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/admin/stats
     * Returns aggregated counts for the admin dashboard:
     * hotels by status, users by role, suspended users.
     * totalBookings and totalRevenue are 0 stubs until M2 is integrated.
     */
    @GetMapping("/stats")
    @Operation(
            summary = "Admin dashboard stats",
            description = "Returns hotel counts by status, user counts by role, and booking/revenue totals (stubs for Step 1)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats returned successfully"),
            @ApiResponse(responseCode = "403", description = "Not an admin")
    })
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}