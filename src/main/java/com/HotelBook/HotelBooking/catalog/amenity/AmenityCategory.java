package com.HotelBook.HotelBooking.catalog.amenity;


/**
 * Groups amenities into logical categories for filtering and icon display.
 * Used by HotelAmenity entity and the frontend filter panel.
 */
public enum AmenityCategory {

    WELLNESS,       // pool, gym, spa, sauna, hot tub
    DINING,         // restaurant, bar, café, room service
    TRANSPORT,      // airport shuttle, car rental, taxi service
    CONNECTIVITY,   // free WiFi, business center, printer
    PARKING,        // free parking, valet, garage
    FAMILY,         // kids club, babysitting, playground, crib
    BUSINESS        // meeting rooms, conference hall, projector
}