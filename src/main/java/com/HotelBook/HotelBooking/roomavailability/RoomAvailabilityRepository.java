package com.HotelBook.HotelBooking.roomavailability;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, UUID> {


    Optional<RoomAvailability> findByRoomIdAndDate(UUID roomId, LocalDate date);

    List<RoomAvailability> findByRoomIdAndDateBetween(UUID roomId, LocalDate start, LocalDate end);

    @Query("SELECT COUNT(a) FROM RoomAvailability a " +
            "WHERE a.roomId = :roomId " +
            "AND a.date >= :checkIn AND a.date < :checkOut " +
            "AND a.blockedCount >= :quantity")
    long countFullyBlockedDates(
            @Param("roomId")   UUID roomId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("quantity") int quantity);

    @Modifying
    @Query("DELETE FROM RoomAvailability a WHERE a.bookingId = :bookingId")
    void deleteByBookingId(@Param("bookingId") UUID bookingId);

    @Query("SELECT a FROM RoomAvailability a " +
            "WHERE a.roomId = :roomId AND a.date IN :dates")
    List<RoomAvailability> findByRoomIdAndDateIn(
            @Param("roomId") UUID roomId,
            @Param("dates") List<LocalDate> dates);


    List<RoomAvailability> findByRoomIdOrderByDateAsc(UUID roomId);


    long countByRoomId(UUID roomId);
}
