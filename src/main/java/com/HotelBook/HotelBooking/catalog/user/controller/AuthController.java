package com.HotelBook.HotelBooking.catalog.user.controller;

import com.HotelBook.HotelBooking.catalog.user.dto.request.*;
import com.HotelBook.HotelBooking.catalog.user.dto.response.*;
import com.HotelBook.HotelBooking.catalog.user.entity.User;
import com.HotelBook.HotelBooking.catalog.user.enums.UserRole;
import com.HotelBook.HotelBooking.catalog.user.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration, login, and profile management")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    // ── POST /api/auth/register ────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a Customer or Hotel Manager account and returns a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Prevent self-registration as ADMIN — admins are seeded manually
        if (request.getRole() == UserRole.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── POST /api/auth/login ───────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user credentials and returns a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ── POST /api/auth/logout ──────────────────────────────────────────────────

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout", description = "Stateless logout — client should discard the token")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        // Strip "Bearer " prefix if present before passing to service
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/auth/me ───────────────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user", description = "Returns the authenticated user's profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        UserResponse response = userService.getCurrentUser(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // ── PATCH /api/auth/me ─────────────────────────────────────────────────────

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update profile", description = "Updates the authenticated user's name, phone, nationality, or date of birth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    // ── PATCH /api/auth/me/password ────────────────────────────────────────────

    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Changes the authenticated user's password after verifying the current one")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or passwords don't match"),
            @ApiResponse(responseCode = "401", description = "Not authenticated or wrong current password")
    })
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
