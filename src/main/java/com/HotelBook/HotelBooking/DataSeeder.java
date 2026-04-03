package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.room.*;
import com.HotelBook.HotelBooking.roomamenity.*;
import com.HotelBook.HotelBooking.roomavailability.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final RoomAmenityRepository roomAmenityRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    private static final java.util.UUID HOTEL_GRAND_AZURE =
            java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Override
    @Transactional
    public void run(String... args) {

        System.out.println("🌱 DataSeeder starting...");

        if (roomRepository.count() > 0) {
            System.out.println("Rooms already exist — skipping seed.");
            return;
        }

        seedRooms();

        System.out.println("✅ DataSeeder finished.");
    }

    private void seedRooms() {

        // =========================
        // ROOM 1
        // =========================
        Room standardKing = Room.builder()
                .hotelId(HOTEL_GRAND_AZURE)
                .name("Standard King")
                .type(RoomType.STANDARD)
                .bedType(BedType.KING)
                .description("Comfortable standard room with king bed and city view.")
                .maxAdults(2)
                .maxChildren(1)
                .quantity(5)
                .sizeSqm(new BigDecimal("28.00"))
                .floor(3)
                .view(RoomView.CITY)
                .price(new BigDecimal("120.00"))
                .build();

        roomRepository.save(standardKing);

        seedAmenities(standardKing);
        seedAvailability(standardKing);


        // =========================
        // ROOM 2
        // =========================
        Room deluxeSuite = Room.builder()
                .hotelId(HOTEL_GRAND_AZURE)
                .name("Deluxe Sea Suite")
                .type(RoomType.SUITE)
                .bedType(BedType.KING)
                .description("Luxury suite with panoramic sea view.")
                .maxAdults(2)
                .maxChildren(2)
                .quantity(3)
                .sizeSqm(new BigDecimal("65.00"))
                .floor(8)
                .view(RoomView.SEA)
                .price(new BigDecimal("250.00"))
                .build();

        roomRepository.save(deluxeSuite);

        seedAmenities(deluxeSuite);
        seedAvailability(deluxeSuite);


        // =========================
        // ROOM 3
        // =========================
        Room familyRoom = Room.builder()
                .hotelId(HOTEL_GRAND_AZURE)
                .name("Family Bunk Room")
                .type(RoomType.FAMILY)
                .bedType(BedType.BUNK)
                .description("Family room with bunk beds and garden view.")
                .maxAdults(2)
                .maxChildren(3)
                .quantity(4)
                .sizeSqm(new BigDecimal("45.00"))
                .floor(2)
                .view(RoomView.GARDEN)
                .price(new BigDecimal("180.00"))
                .build();

        roomRepository.save(familyRoom);

        seedAmenities(familyRoom);
        seedAvailability(familyRoom);

    }

    private void seedAmenities(Room room) {

        RoomAmenity wifi = RoomAmenity.builder()
                .room(room)
                .name("Free WiFi")
                .category(AmenityCategory.TECH)
                .icon("wifi")
                .build();

        RoomAmenity ac = RoomAmenity.builder()
                .room(room)
                .name("Air Conditioning")
                .category(AmenityCategory.COMFORT)
                .icon("ac")
                .build();

        RoomAmenity tv = RoomAmenity.builder()
                .room(room)
                .name("Smart TV")
                .category(AmenityCategory.TECH)
                .icon("tv")
                .build();

        roomAmenityRepository.save(wifi);
        roomAmenityRepository.save(ac);
        roomAmenityRepository.save(tv);
    }

    private void seedAvailability(Room room) {

        RoomAvailability today = RoomAvailability.builder()
                .roomId(room.getId())
                .date(LocalDate.now())
                .blockedReason(RoomAvailability.BlockedReason.MANAGER_BLOCK)
                .blockedCount(1)
                .build();

        RoomAvailability tomorrow = RoomAvailability.builder()
                .roomId(room.getId())
                .date(LocalDate.now().plusDays(1))
                .blockedReason(RoomAvailability.BlockedReason.MAINTENANCE)
                .blockedCount(1)
                .build();

        roomAvailabilityRepository.save(today);
        roomAvailabilityRepository.save(tomorrow);
    }
}
