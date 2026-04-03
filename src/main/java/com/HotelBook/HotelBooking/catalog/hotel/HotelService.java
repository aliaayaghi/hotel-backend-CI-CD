package com.HotelBook.HotelBooking.catalog.hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HotelService {

    // ── Public (no auth required) ─────────────────────────────────────────────
    Page<HotelSummaryResponse> searchHotels(HotelSearchRequest filter, Pageable pageable);

    HotelResponse getHotelById(UUID hotelId);

    // ── Manager ───────────────────────────────────────────────────────────────
    HotelResponse createHotel(UUID managerId, CreateHotelRequest request);

    HotelResponse updateHotel(UUID managerId, UUID hotelId, UpdateHotelRequest request);

    void suspendHotel(UUID managerId, UUID hotelId);

    Page<HotelResponse> getMyHotels(UUID managerId, Pageable pageable);
}
