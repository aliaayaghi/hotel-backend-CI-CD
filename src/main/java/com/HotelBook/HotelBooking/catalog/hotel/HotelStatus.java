package com.HotelBook.HotelBooking.catalog.hotel;

/**
 * Lifecycle status for a Hotel.
 *
 * Flow:
 *   PENDING  → admin approves  → ACTIVE
 *   PENDING  → admin rejects   → REJECTED
 *   ACTIVE   → manager/admin   → SUSPENDED
 *   REJECTED → manager resubmits (reset to PENDING by manager, Step 2+)
 */
public enum HotelStatus {
    PENDING,    // newly created, awaiting admin review
    ACTIVE,     // live and visible in public search
    REJECTED,   // admin rejected — hidden from public
    SUSPENDED   // soft-deleted by manager or admin — hidden from public
}