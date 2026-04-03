package com.HotelBook.HotelBooking.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {


    List<Booking> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<Booking> findByIdAndCustomerId(UUID id, UUID customerId);
    List<Booking> findByCustomerIdAndStatus(UUID customerId, BookingStatus status);
    List<Booking> findByRoomIdOrderByCheckInDateAsc(UUID roomId);
    List<Booking> findByHotelIdOrderByCheckInDateAsc(UUID hotelId);
    List<Booking> findByHotelIdAndStatus(UUID hotelId, BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.roomCount), 0) FROM Booking b " +
            "WHERE b.roomId = :roomId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.checkInDate < :checkOut " +
            "AND b.checkOutDate > :checkIn")
    int countConfirmedRoomsInDateRange(
            @Param("roomId")    UUID roomId,
            @Param("checkIn")   LocalDate checkIn,
            @Param("checkOut")  LocalDate checkOut);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.roomId = :roomId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkInDate < :checkOut " +
            "AND b.checkOutDate > :checkIn")
    List<Booking> findActiveBookingsInRange(
            @Param("roomId")   UUID roomId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.hotelId = :hotelId " +
            "AND b.checkInDate = :today " +
            "AND b.status = 'CONFIRMED'")
    List<Booking> findTodaysArrivals(
            @Param("hotelId") UUID hotelId,
            @Param("today")   LocalDate today);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.hotelId = :hotelId " +
            "AND b.checkOutDate = :today " +
            "AND b.status = 'CONFIRMED'")
    List<Booking> findTodaysDepartures(
            @Param("hotelId") UUID hotelId,
            @Param("today")   LocalDate today);
}
