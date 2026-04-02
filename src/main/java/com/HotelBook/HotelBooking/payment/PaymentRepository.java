package com.HotelBook.HotelBooking.payment;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {


    Optional<Payment> findByBookingId(UUID bookingId);


    boolean existsByBookingId(UUID bookingId);

    Optional<Payment> findByBookingIdAndCustomerId(UUID bookingId, UUID customerId);

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Payment> findByStatus(PaymentStatus status);
}
