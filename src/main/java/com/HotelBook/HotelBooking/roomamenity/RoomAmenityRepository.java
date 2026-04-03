package com.HotelBook.HotelBooking.roomamenity;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, UUID> {


    List<RoomAmenity> findByRoomId(UUID roomId);

    Optional<RoomAmenity> findByIdAndRoomId(UUID id, UUID roomId);

    boolean existsByRoomIdAndNameIgnoreCase(UUID roomId, String name);

    List<RoomAmenity> findByRoomIdAndCategory(UUID roomId, AmenityCategory category);
}
