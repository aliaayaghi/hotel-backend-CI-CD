package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Admin.AdminDashboardResponse;
import com.HotelBook.HotelBooking.Admin.AdminServiceImpl;
import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.Hotel.*;
import com.HotelBook.HotelBooking.User.dto.response.UserResponse;
import com.HotelBook.HotelBooking.User.entity.User;
import com.HotelBook.HotelBooking.User.enums.UserRole;
import com.HotelBook.HotelBooking.User.mapper.UserMapper;
import com.HotelBook.HotelBooking.User.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminServiceImpl Unit Tests")
class AdminServiceImplTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private UserRepository userRepository;
    @Mock private HotelMapper hotelMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    private UUID hotelId;
    private UUID userId;
    private Hotel pendingHotel;
    private User activeCustomer;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        userId  = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        pendingHotel = Hotel.builder()
                .id(hotelId)
                .name("Desert Inn")
                .status(HotelStatus.PENDING)
                .build();

        activeCustomer = User.builder()
                .id(userId)
                .name("Customer")
                .email("cust@hotel.com")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getAllHotels
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAllHotels()")
    class GetAllHotels {

        @Test
        @DisplayName("should filter by status when status is provided")
        void shouldFilterByStatus() {
            given(hotelRepository.findAllByStatus(eq(HotelStatus.PENDING), eq(pageable)))
                    .willReturn(new PageImpl<>(List.of(pendingHotel)));
            given(hotelMapper.toHotelResponse(any())).willReturn(HotelResponse.builder().build());

            Page<HotelResponse> result = adminService.getAllHotels(HotelStatus.PENDING, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(hotelRepository).findAllByStatus(HotelStatus.PENDING, pageable);
            verify(hotelRepository, never()).findAll(pageable);
        }

        @Test
        @DisplayName("should return all hotels when status is null")
        void shouldReturnAllWhenStatusNull() {
            given(hotelRepository.findAll(pageable))
                    .willReturn(new PageImpl<>(List.of(pendingHotel)));
            given(hotelMapper.toHotelResponse(any())).willReturn(HotelResponse.builder().build());

            Page<HotelResponse> result = adminService.getAllHotels(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(hotelRepository).findAll(pageable);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  approveHotel
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("approveHotel()")
    class ApproveHotel {

        @Test
        @DisplayName("should set hotel status to ACTIVE")
        void shouldApproveHotel() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(pendingHotel));
            given(hotelRepository.save(any())).willReturn(pendingHotel);
            given(hotelMapper.toHotelResponse(any())).willReturn(HotelResponse.builder().build());

            adminService.approveHotel(hotelId);

            assertThat(pendingHotel.getStatus()).isEqualTo(HotelStatus.ACTIVE);
            verify(hotelRepository).save(pendingHotel);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when hotel does not exist")
        void shouldThrowWhenHotelNotFound() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.approveHotel(hotelId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  rejectHotel
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("rejectHotel()")
    class RejectHotel {

        @Test
        @DisplayName("should set hotel status to REJECTED")
        void shouldRejectHotel() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(pendingHotel));
            given(hotelRepository.save(any())).willReturn(pendingHotel);
            given(hotelMapper.toHotelResponse(any())).willReturn(HotelResponse.builder().build());

            adminService.rejectHotel(hotelId, "Fake address");

            assertThat(pendingHotel.getStatus()).isEqualTo(HotelStatus.REJECTED);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when hotel not found")
        void shouldThrowWhenNotFound() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.rejectHotel(hotelId, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  deleteHotel
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteHotel()")
    class DeleteHotel {

        @Test
        @DisplayName("should call repository delete")
        void shouldDeleteHotel() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(pendingHotel));

            adminService.deleteHotel(hotelId);

            verify(hotelRepository).delete(pendingHotel);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when hotel not found")
        void shouldThrowWhenNotFound() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.deleteHotel(hotelId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  suspendUser
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("suspendUser()")
    class SuspendUser {

        @Test
        @DisplayName("should set user isActive to false")
        void shouldSuspendUser() {
            given(userRepository.findById(userId)).willReturn(Optional.of(activeCustomer));
            given(userRepository.save(any())).willReturn(activeCustomer);
            given(userMapper.toUserResponse(any())).willReturn(UserResponse.builder().build());

            adminService.suspendUser(userId, "Violation of TOS");

            assertThat(activeCustomer.isActive()).isFalse();
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when suspending an ADMIN")
        void shouldThrowWhenSuspendingAdmin() {
            User admin = User.builder()
                    .id(userId)
                    .name("Admin")
                    .email("admin@hotel.com")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();
            given(userRepository.findById(userId)).willReturn(Optional.of(admin));

            assertThatThrownBy(() -> adminService.suspendUser(userId, "reason"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Admin accounts cannot be suspended");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.suspendUser(userId, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  unsuspendUser
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("unsuspendUser()")
    class UnsuspendUser {

        @Test
        @DisplayName("should restore user isActive to true")
        void shouldUnsuspendUser() {
            activeCustomer.setActive(false);
            given(userRepository.findById(userId)).willReturn(Optional.of(activeCustomer));
            given(userRepository.save(any())).willReturn(activeCustomer);
            given(userMapper.toUserResponse(any())).willReturn(UserResponse.builder().build());

            adminService.unsuspendUser(userId);

            assertThat(activeCustomer.isActive()).isTrue();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> adminService.unsuspendUser(userId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getDashboardStats
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getDashboardStats()")
    class GetDashboardStats {

        @Test
        @DisplayName("should return aggregated counts from repositories")
        void shouldReturnDashboardStats() {
            given(hotelRepository.count()).willReturn(10L);
            given(hotelRepository.countByStatus(HotelStatus.PENDING)).willReturn(2L);
            given(hotelRepository.countByStatus(HotelStatus.ACTIVE)).willReturn(6L);
            given(hotelRepository.countByStatus(HotelStatus.REJECTED)).willReturn(1L);
            given(hotelRepository.countByStatus(HotelStatus.SUSPENDED)).willReturn(1L);
            given(userRepository.count()).willReturn(50L);
            given(userRepository.countByRole(UserRole.CUSTOMER)).willReturn(40L);
            given(userRepository.countByRole(UserRole.HOTEL_MANAGER)).willReturn(9L);
            given(userRepository.countByIsActiveFalse()).willReturn(3L);

            AdminDashboardResponse stats = adminService.getDashboardStats();

            assertThat(stats.getTotalHotels()).isEqualTo(10L);
            assertThat(stats.getPendingHotels()).isEqualTo(2L);
            assertThat(stats.getActiveHotels()).isEqualTo(6L);
            assertThat(stats.getTotalUsers()).isEqualTo(50L);
            assertThat(stats.getTotalCustomers()).isEqualTo(40L);
            assertThat(stats.getSuspendedUsers()).isEqualTo(3L);
            // Booking stubs are 0 for Step 1
            assertThat(stats.getTotalBookings()).isEqualTo(0L);
            assertThat(stats.getTotalRevenue()).isEqualTo(0.0);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getAllUsers
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("should return paginated user list")
        void shouldReturnUsers() {
            given(userRepository.findAll(pageable))
                    .willReturn(new PageImpl<>(List.of(activeCustomer)));
            given(userMapper.toUserResponse(any())).willReturn(UserResponse.builder().build());

            Page<UserResponse> result = adminService.getAllUsers(pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }
}

