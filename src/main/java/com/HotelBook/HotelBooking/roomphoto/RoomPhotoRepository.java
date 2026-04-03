package com.HotelBook.HotelBooking.roomphoto;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface RoomPhotoRepository extends JpaRepository<RoomPhoto, UUID> {


    List<RoomPhoto> findByRoomIdOrderByDisplayOrderAsc(UUID roomId);

    Optional<RoomPhoto> findByIdAndRoomId(UUID id, UUID roomId);

    @Query("SELECT COALESCE(MAX(p.displayOrder), -1) FROM RoomPhoto p WHERE p.room.id = :roomId")
    int findMaxDisplayOrderByRoomId(@Param("roomId") UUID roomId);

    long countByRoomId(UUID roomId);

    boolean existsByRoomIdAndUrl(UUID roomId, String url);
}
