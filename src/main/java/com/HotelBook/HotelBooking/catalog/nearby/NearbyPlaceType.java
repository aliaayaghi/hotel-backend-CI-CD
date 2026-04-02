package com.HotelBook.HotelBooking.catalog.nearby;

/**
 * Type of point of interest near a hotel.
 * Used for categorizing nearby places and for frontend icons/filters.
 */
public enum NearbyPlaceType {

    AIRPORT,        // international or domestic airport
    BEACH,          // sea, lake, or river beach
    MALL,           // shopping center or market
    RESTAURANT,     // standalone restaurant, food court
    LANDMARK,       // tourist attraction, monument, museum
    HOSPITAL        // hospital, clinic, pharmacy
}