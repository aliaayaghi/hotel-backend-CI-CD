package com.HotelBook.HotelBooking.room;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByHotelIdAndIsActiveTrue(UUID hotelId);
    List<Room> findByHotelId(UUID hotelId);
    Optional<Room> findByIdAndHotelId(UUID id, UUID hotelId);
    boolean existsByHotelIdAndIsActiveTrue(UUID hotelId);

    @Query("SELECT r FROM Room r WHERE r.hotelId = :hotelId AND r.isActive = true " +
            "AND r.maxAdults >= :adults AND r.maxChildren >= :children")
    List<Room> findAvailableByCapacity(@Param("hotelId") UUID hotelId,
                                       @Param("adults") int adults,
                                       @Param("children") int children);
}
