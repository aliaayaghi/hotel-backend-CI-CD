package com.HotelBook.HotelBooking.catalog.policy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInPolicyRepository extends JpaRepository<CheckInPolicy, UUID> {

    Optional<CheckInPolicy> findByHotelId(UUID hotelId);

    boolean existsByHotelId(UUID hotelId);
}