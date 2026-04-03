package com.HotelBook.HotelBooking.savedhotel;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface SavedHotelRepository extends JpaRepository<SavedHotel, UUID> {


    List<SavedHotel> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<SavedHotel> findByCustomerIdAndHotelId(UUID customerId, UUID hotelId);
    boolean existsByCustomerIdAndHotelId(UUID customerId, UUID hotelId);
    void deleteByCustomerIdAndHotelId(UUID customerId, UUID hotelId);
    long countByHotelId(UUID hotelId);
    long countByCustomerId(UUID customerId);
}
