package com.HotelBook.HotelBooking.catalog.user.repository;

import com.HotelBook.HotelBooking.catalog.user.entity.User;
import com.HotelBook.HotelBooking.catalog.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ── Used by AuthService ──────────────────────────────────────────────────
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ── Used by AdminService ─────────────────────────────────────────────────
    List<User> findAllByRole(UserRole role);

    List<User> findAllByRoleAndIsActive(UserRole role, boolean isActive);

    long countByRole(UserRole role);           // dashboard: count customers / managers

    long countByIsActiveFalse();               // dashboard: count suspended users
}