package com.HotelBook.HotelBooking.catalog.hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, UUID>,
        JpaSpecificationExecutor<Hotel> {           // enables dynamic search filters

    // ── Admin ─────────────────────────────────────────────────────────────────
    Page<Hotel> findAllByStatus(HotelStatus status, Pageable pageable);

    long countByStatus(HotelStatus status);

    // ── Manager ownership ─────────────────────────────────────────────────────
    Page<Hotel> findAllByManager_Id(UUID managerId, Pageable pageable);

    List<Hotel> findAllByManager_Id(UUID managerId);

    boolean existsByIdAndManager_Id(UUID hotelId, UUID managerId);

    Optional<Hotel> findByIdAndManager_Id(UUID hotelId, UUID managerId);

    // ── Public search ─────────────────────────────────────────────────────────
    Page<Hotel> findAllByStatusAndCityContainingIgnoreCase(
            HotelStatus status, String city, Pageable pageable);

    Page<Hotel> findAllByStatusAndCountryCode(
            HotelStatus status, String countryCode, Pageable pageable);

    Page<Hotel> findAllByStatusAndType(
            HotelStatus status, HotelType type, Pageable pageable);

    Page<Hotel> findAllByStatusAndStarRating(
            HotelStatus status, int starRating, Pageable pageable);

    // ── Full-text style search across name, city, country ─────────────────────
    @Query("""
        SELECT h FROM Hotel h
        WHERE h.status = :status
          AND (
               LOWER(h.name)        LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(h.city)        LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(h.countryCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(h.address)     LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        """)
    Page<Hotel> searchByKeyword(
            @Param("status") HotelStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    // ── Dashboard ─────────────────────────────────────────────────────────────
    long countByManager_Id(UUID managerId);
}
