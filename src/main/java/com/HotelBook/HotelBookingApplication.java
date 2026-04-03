package com.HotelBook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * HotelBookingApplication — the Spring Boot entry point.
 *
 * ─── CRITICAL: DUAL COMPONENT SCAN ───────────────────────────────────────────
 *
 * This project has TWO package roots that must both be scanned:
 *
 * 1. com.HotelBook.HotelBooking  — your main application package (this file,
 *                                  SecurityConfig, and any future config classes)
 *
 * 2. com.hotelapp                — all the generated code:
 *                                  com.hotelapp.room, com.hotelapp.booking,
 *                                  com.hotelapp.payment, com.hotelapp.availability,
 *                                  com.hotelapp.pricing, com.hotelapp.cancellation,
 *                                  com.hotelapp.roomphoto, com.hotelapp.roomamenity,
 *                                  com.hotelapp.roomaccessibility, com.hotelapp.common
 *
 * Without scanBasePackages, Spring Boot ONLY scans com.HotelBook.HotelBooking
 * and misses all the @Service, @Repository, @RestController classes in com.hotelapp.
 * The application would start but ALL endpoints would return 404.
 *
 * WHY TWO ROOTS?
 * The pom.xml groupId is com.HotelBook, so your main class lives in
 * com.HotelBook.HotelBooking. But the generated service files use the
 * conventional com.hotelapp package (lowercase, following Java naming conventions).
 * Both need to be scanned.
 *
 * ─── LONG-TERM FIX (OPTIONAL) ────────────────────────────────────────────────
 *
 * If you want to eliminate the dual-scan, rename all generated packages from
 * com.hotelapp.* to com.HotelBook.HotelBooking.* — but this is a large rename
 * and the dual scan works perfectly fine for the project's lifetime.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.HotelBook.HotelBooking",   // SecurityConfig, and your own classes
        "com.hotelapp"                   // All generated service/controller/repository classes
})
public class HotelBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelBookingApplication.class, args);
    }
}