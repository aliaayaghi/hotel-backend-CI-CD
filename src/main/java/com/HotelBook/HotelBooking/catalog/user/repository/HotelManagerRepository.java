package com.HotelBook.HotelBooking.catalog.user.repository;


import com.HotelBook.HotelBooking.catalog.user.entity.HotelManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotelManagerRepository extends JpaRepository<HotelManager, UUID> {

    Optional<HotelManager> findByUser_Id(UUID userId);
}
