package com.HotelBook.HotelBooking.cancellation;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, UUID> {


    List<CancellationPolicy> findByRoomId(UUID roomId);

    Optional<CancellationPolicy> findByRoomIdAndIsDefaultTrue(UUID roomId);

    Optional<CancellationPolicy> findByIdAndRoomId(UUID id, UUID roomId);

    boolean existsByRoomId(UUID roomId);

    List<CancellationPolicy> findByHotelIdAndRoomIdIsNull(UUID hotelId);

    Optional<CancellationPolicy> findByHotelIdAndRoomIdIsNullAndIsDefaultTrue(UUID hotelId);


    Optional<CancellationPolicy> findByIdAndHotelIdAndRoomIdIsNull(UUID id, UUID hotelId);


    List<CancellationPolicy> findByHotelId(UUID hotelId);

    List<CancellationPolicy> findByHotelIdAndIsDefaultTrue(UUID hotelId);


    @Modifying(clearAutomatically = true)
    @Query("UPDATE CancellationPolicy p SET p.isDefault = false WHERE p.roomId = :roomId")
    void clearDefaultForRoom(@Param("roomId") UUID roomId);


    @Modifying(clearAutomatically = true)
    @Query("UPDATE CancellationPolicy p SET p.isDefault = false " +
            "WHERE p.hotelId = :hotelId AND p.roomId IS NULL")
    void clearDefaultForHotel(@Param("hotelId") UUID hotelId);


    long countByRoomId(UUID roomId);
}
