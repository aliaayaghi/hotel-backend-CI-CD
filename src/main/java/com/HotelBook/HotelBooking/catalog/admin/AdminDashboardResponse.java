package com.HotelBook.HotelBooking.catalog.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardResponse {

    // ── Hotels ──────────────────────────────────────────────────────────────
    private long totalHotels;
    private long pendingHotels;
    private long activeHotels;
    private long rejectedHotels;
    private long suspendedHotels;

    // ── Users ────────────────────────────────────────────────────────────────
    private long totalUsers;
    private long totalCustomers;
    private long totalManagers;
    private long suspendedUsers;

    // ── Bookings & Revenue — stubs for Step 1, M2 fills real values later ───
    private long totalBookings;
    private double totalRevenue;
}

