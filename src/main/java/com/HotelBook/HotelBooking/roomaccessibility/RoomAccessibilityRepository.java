package com.HotelBook.HotelBooking.roomaccessibility;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface RoomAccessibilityRepository extends JpaRepository<RoomAccessibility, UUID> {


    List<RoomAccessibility> findByRoomId(UUID roomId);

    Optional<RoomAccessibility> findByIdAndRoomId(UUID id, UUID roomId);

    List<RoomAccessibility> findByRoomIdAndIsAvailableTrue(UUID roomId);

    boolean existsByRoomIdAndFeatureIgnoreCase(UUID roomId, String feature);
}