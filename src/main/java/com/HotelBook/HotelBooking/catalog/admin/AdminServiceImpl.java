package com.HotelBook.HotelBooking.catalog.admin;


import com.HotelBook.HotelBooking.catalog.hotel.*;
import com.HotelBook.HotelBooking.catalog.user.dto.response.UserResponse;
import com.HotelBook.HotelBooking.catalog.user.entity.User;
import com.HotelBook.HotelBooking.catalog.user.enums.UserRole;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.catalog.user.mapper.UserMapper;
import com.HotelBook.HotelBooking.catalog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelMapper hotelMapper;
    private final UserMapper userMapper;

    // ── Hotel management ──────────────────────────────────────────────────────

    /**
     * List all hotels, optionally filtered by status.
     * Passing null for status returns ALL hotels regardless of status —
     * admins need to see pending, rejected, and suspended hotels too.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> getAllHotels(HotelStatus status, Pageable pageable) {
        Page<Hotel> hotels = (status != null)
                ? hotelRepository.findAllByStatus(status, pageable)
                : hotelRepository.findAll(pageable);

        return hotels.map(hotelMapper::toHotelResponse);
    }

    /**
     * Approve a hotel — set status PENDING → ACTIVE so it appears in public search.
     * Throws ResourceNotFoundException if the hotel does not exist.
     */
    @Override
    @Transactional
    public HotelResponse approveHotel(UUID hotelId) {
        Hotel hotel = findHotelById(hotelId);

        if (hotel.getStatus() == HotelStatus.ACTIVE) {
            log.warn("Hotel {} is already active — approve called again", hotelId);
        }

        hotel.setStatus(HotelStatus.ACTIVE);
        hotel = hotelRepository.save(hotel);

        log.info("Admin approved hotel: {} ({})", hotel.getName(), hotelId);
        return hotelMapper.toHotelResponse(hotel);
    }

    /**
     * Reject a hotel — set status to REJECTED and store the reason
     * in the hotel's overview field suffix or a dedicated rejectionReason field.
     *
     * NOTE: If you add a rejectionReason column to Hotel, set it here.
     *       For Step 1 we log the reason and it is returned in the response message.
     */
    @Override
    @Transactional
    public HotelResponse rejectHotel(UUID hotelId, String reason) {
        Hotel hotel = findHotelById(hotelId);

        hotel.setStatus(HotelStatus.REJECTED);
        hotel = hotelRepository.save(hotel);

        log.info("Admin rejected hotel: {} ({}) — reason: {}", hotel.getName(), hotelId, reason);
        return hotelMapper.toHotelResponse(hotel);
    }

    /**
     * Hard-delete a hotel and all its child records.
     * Cascading deletes are handled by DB foreign-key ON DELETE CASCADE,
     * or by @Cascade on the Hotel entity relationships.
     *
     * Only admins can hard-delete. Managers soft-delete (status = SUSPENDED).
     */
    @Override
    @Transactional
    public void deleteHotel(UUID hotelId) {
        Hotel hotel = findHotelById(hotelId);
        hotelRepository.delete(hotel);
        log.info("Admin hard-deleted hotel: {} ({})", hotel.getName(), hotelId);
    }

    // ── User management ───────────────────────────────────────────────────────

    /**
     * List all users with pagination.
     * Returns base UserResponse — role-specific details not included here.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    /**
     * Suspend a user — set isActive = false.
     * The user will be rejected by Spring Security on next request
     * because User.isAccountNonLocked() returns isActive.
     *
     * Any existing JWT tokens they hold will fail the UserDetails check
     * in JwtAuthenticationFilter once the DB record is updated.
     */
    @Override
    @Transactional
    public UserResponse suspendUser(UUID userId, String reason) {
        User user = findUserById(userId);

        if (!user.isActive()) {
            log.warn("User {} is already suspended — suspend called again", userId);
        }

        // Prevent admins from suspending other admins
        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be suspended through this endpoint");
        }

        user.setActive(false);
        user = userRepository.save(user);

        log.info("Admin suspended user: {} ({}) — reason: {}", user.getEmail(), userId, reason);
        return userMapper.toUserResponse(user);
    }

    /**
     * Unsuspend a user — restore isActive = true.
     * Their next login will succeed again.
     */
    @Override
    @Transactional
    public UserResponse unsuspendUser(UUID userId) {
        User user = findUserById(userId);

        if (user.isActive()) {
            log.warn("User {} is already active — unsuspend called on active user", userId);
        }

        user.setActive(true);
        user = userRepository.save(user);

        log.info("Admin unsuspended user: {} ({})", user.getEmail(), userId);
        return userMapper.toUserResponse(user);
    }

    // ── Dashboard stats ───────────────────────────────────────────────────────

    /**
     * Aggregate counts for the admin dashboard.
     *
     * totalBookings and totalRevenue are 0 stubs for Step 1.
     * M2 will expose a BookingStatsService bean that this method can call in Step 2:
     *
     *   long totalBookings = bookingStatsService.getTotalBookings();
     *   double totalRevenue = bookingStatsService.getTotalRevenue();
     */
    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        return AdminDashboardResponse.builder()
                // Hotel counts
                .totalHotels(hotelRepository.count())
                .pendingHotels(hotelRepository.countByStatus(HotelStatus.PENDING))
                .activeHotels(hotelRepository.countByStatus(HotelStatus.ACTIVE))
                .rejectedHotels(hotelRepository.countByStatus(HotelStatus.REJECTED))
                .suspendedHotels(hotelRepository.countByStatus(HotelStatus.SUSPENDED))
                // User counts
                .totalUsers(userRepository.count())
                .totalCustomers(userRepository.countByRole(UserRole.CUSTOMER))
                .totalManagers(userRepository.countByRole(UserRole.HOTEL_MANAGER))
                .suspendedUsers(userRepository.countByIsActiveFalse())
                // Booking/revenue stubs — M2 fills these in Step 2
                .totalBookings(0L)
                .totalRevenue(0.0)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Hotel findHotelById(UUID hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
