package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Common.exception.HotelNotFoundException;
import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.Common.exception.UnauthorizedHotelAccessException;
import com.HotelBook.HotelBooking.Hotel.*;
import com.HotelBook.HotelBooking.User.entity.HotelManager;
import com.HotelBook.HotelBooking.User.entity.User;
import com.HotelBook.HotelBooking.User.enums.UserRole;
import com.HotelBook.HotelBooking.User.repository.HotelManagerRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("HotelServiceImpl Unit Tests")
class HotelServiceImplTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private HotelManagerRepository hotelManagerRepository;
    @Mock private HotelMapper hotelMapper;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private UUID managerId;
    private UUID hotelId;
    private HotelManager manager;
    private Hotel activeHotel;
    private HotelResponse hotelResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        managerId = UUID.randomUUID();
        hotelId   = UUID.randomUUID();

        User managerUser = User.builder()
                .id(managerId)
                .name("Manager Bob")
                .email("bob@hotel.com")
                .role(UserRole.HOTEL_MANAGER)
                .isActive(true)
                .build();

        manager = HotelManager.builder()
                .id(managerId)
                .user(managerUser)
                .build();

        activeHotel = Hotel.builder()
                .id(hotelId)
                .name("Grand Palace")
                .status(HotelStatus.ACTIVE)
                .manager(manager)
                .city("Nablus")
                .countryCode("PS")
                .address("Main St")
                .starRating(4)
                .build();

        hotelResponse = HotelResponse.builder()
                .id(hotelId)
                .name("Grand Palace")
                .status(HotelStatus.ACTIVE)
                .city("Nablus")
                .build();

        pageable = PageRequest.of(0, 10);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getHotelById (public)
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getHotelById()")
    class GetHotelById {

        @Test
        @DisplayName("should return hotel response for an ACTIVE hotel")
        void shouldReturnActiveHotel() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));
            given(hotelMapper.toHotelResponse(activeHotel)).willReturn(hotelResponse);

            HotelResponse result = hotelService.getHotelById(hotelId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(hotelId);
        }

        @Test
        @DisplayName("should throw HotelNotFoundException when hotel does not exist")
        void shouldThrowWhenHotelNotFound() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> hotelService.getHotelById(hotelId))
                    .isInstanceOf(HotelNotFoundException.class);
        }

        @Test
        @DisplayName("should throw HotelNotFoundException when hotel is PENDING (hidden from public)")
        void shouldThrowWhenHotelIsPending() {
            activeHotel.setStatus(HotelStatus.PENDING);
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));

            assertThatThrownBy(() -> hotelService.getHotelById(hotelId))
                    .isInstanceOf(HotelNotFoundException.class);
        }

        @Test
        @DisplayName("should throw HotelNotFoundException when hotel is SUSPENDED")
        void shouldThrowWhenHotelIsSuspended() {
            activeHotel.setStatus(HotelStatus.SUSPENDED);
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));

            assertThatThrownBy(() -> hotelService.getHotelById(hotelId))
                    .isInstanceOf(HotelNotFoundException.class);
        }

        @Test
        @DisplayName("should throw HotelNotFoundException when hotel is REJECTED")
        void shouldThrowWhenHotelIsRejected() {
            activeHotel.setStatus(HotelStatus.REJECTED);
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));

            assertThatThrownBy(() -> hotelService.getHotelById(hotelId))
                    .isInstanceOf(HotelNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  searchHotels (public)
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("searchHotels()")
    class SearchHotels {

        @Test
        @DisplayName("should search by keyword when keyword is provided")
        void shouldSearchByKeyword() {
            HotelSearchRequest filter = new HotelSearchRequest();
            filter.setKeyword("Grand");

            Page<Hotel> page = new PageImpl<>(List.of(activeHotel));
            given(hotelRepository.searchByKeyword(HotelStatus.ACTIVE, "Grand", pageable))
                    .willReturn(page);
            given(hotelMapper.toHotelSummaryResponse(activeHotel))
                    .willReturn(HotelSummaryResponse.builder().id(hotelId).name("Grand Palace").build());

            Page<HotelSummaryResponse> result = hotelService.searchHotels(filter, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(hotelRepository).searchByKeyword(HotelStatus.ACTIVE, "Grand", pageable);
        }

        @Test
        @DisplayName("should search by city when city is provided (no keyword)")
        void shouldSearchByCity() {
            HotelSearchRequest filter = new HotelSearchRequest();
            filter.setCity("Nablus");

            given(hotelRepository.findAllByStatusAndCityContainingIgnoreCase(
                    HotelStatus.ACTIVE, "Nablus", pageable))
                    .willReturn(new PageImpl<>(List.of(activeHotel)));
            given(hotelMapper.toHotelSummaryResponse(any())).willReturn(
                    HotelSummaryResponse.builder().id(hotelId).build());

            Page<HotelSummaryResponse> result = hotelService.searchHotels(filter, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return all active hotels when no filters are provided")
        void shouldReturnAllActiveHotelsWhenNoFilter() {
            HotelSearchRequest filter = new HotelSearchRequest();

            given(hotelRepository.findAllByStatus(HotelStatus.ACTIVE, pageable))
                    .willReturn(new PageImpl<>(List.of(activeHotel)));
            given(hotelMapper.toHotelSummaryResponse(any())).willReturn(
                    HotelSummaryResponse.builder().id(hotelId).build());

            Page<HotelSummaryResponse> result = hotelService.searchHotels(filter, pageable);

            assertThat(result).isNotNull();
            verify(hotelRepository).findAllByStatus(HotelStatus.ACTIVE, pageable);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  createHotel
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createHotel()")
    class CreateHotel {

        @Test
        @DisplayName("should create hotel with PENDING status")
        void shouldCreateHotelAsPending() {
            CreateHotelRequest request = new CreateHotelRequest();
            request.setName("New Hotel");
            request.setCity("Ramallah");
            request.setCountryCode("ps");
            request.setAddress("Main Ave");
            request.setStarRating(3);
            request.setType(HotelType.HOTEL);

            given(hotelManagerRepository.findByUser_Id(managerId))
                    .willReturn(Optional.of(manager));
            given(hotelRepository.save(any(Hotel.class))).willAnswer(inv -> {
                Hotel h = inv.getArgument(0);
                h = Hotel.builder()
                        .id(UUID.randomUUID())
                        .manager(h.getManager())
                        .name(h.getName())
                        .status(h.getStatus())
                        .countryCode(h.getCountryCode())
                        .city(h.getCity())
                        .address(h.getAddress())
                        .build();
                return h;
            });
            given(hotelMapper.toHotelResponse(any())).willReturn(hotelResponse);

            HotelResponse result = hotelService.createHotel(managerId, request);

            assertThat(result).isNotNull();
            // Verify it was saved with PENDING status
            verify(hotelRepository).save(argThat(h -> h.getStatus() == HotelStatus.PENDING));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when manager not found")
        void shouldThrowWhenManagerNotFound() {
            given(hotelManagerRepository.findByUser_Id(managerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> hotelService.createHotel(managerId, new CreateHotelRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should uppercase countryCode when creating hotel")
        void shouldUppercaseCountryCode() {
            CreateHotelRequest request = new CreateHotelRequest();
            request.setName("Hotel");
            request.setCity("City");
            request.setCountryCode("ps");
            request.setAddress("Addr");
            request.setStarRating(3);
            request.setType(HotelType.HOTEL);

            given(hotelManagerRepository.findByUser_Id(managerId)).willReturn(Optional.of(manager));
            given(hotelRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(hotelMapper.toHotelResponse(any())).willReturn(hotelResponse);

            hotelService.createHotel(managerId, request);

            verify(hotelRepository).save(argThat(h -> "PS".equals(h.getCountryCode())));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  updateHotel
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateHotel()")
    class UpdateHotel {

        @Test
        @DisplayName("should update hotel name when name is provided")
        void shouldUpdateHotelName() {
            UpdateHotelRequest request = new UpdateHotelRequest();
            request.setName("Updated Name");

            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));
            given(hotelRepository.save(any())).willReturn(activeHotel);
            given(hotelMapper.toHotelResponse(any())).willReturn(hotelResponse);

            hotelService.updateHotel(managerId, hotelId, request);

            assertThat(activeHotel.getName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("should re-submit REJECTED hotel to PENDING when updated")
        void shouldResubmitRejectedHotel() {
            activeHotel.setStatus(HotelStatus.REJECTED);
            UpdateHotelRequest request = new UpdateHotelRequest();
            request.setName("Fixed Name");

            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));
            given(hotelRepository.save(any())).willReturn(activeHotel);
            given(hotelMapper.toHotelResponse(any())).willReturn(hotelResponse);

            hotelService.updateHotel(managerId, hotelId, request);

            assertThat(activeHotel.getStatus()).isEqualTo(HotelStatus.PENDING);
        }

        @Test
        @DisplayName("should throw UnauthorizedHotelAccessException when manager does not own hotel")
        void shouldThrowWhenManagerDoesNotOwnHotel() {
            UUID otherManagerId = UUID.randomUUID();
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));

            assertThatThrownBy(() -> hotelService.updateHotel(otherManagerId, hotelId, new UpdateHotelRequest()))
                    .isInstanceOf(UnauthorizedHotelAccessException.class);
        }

        @Test
        @DisplayName("should throw HotelNotFoundException when hotel does not exist")
        void shouldThrowWhenHotelNotFound() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> hotelService.updateHotel(managerId, hotelId, new UpdateHotelRequest()))
                    .isInstanceOf(HotelNotFoundException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  suspendHotel
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("suspendHotel()")
    class SuspendHotel {

        @Test
        @DisplayName("should set hotel status to SUSPENDED")
        void shouldSuspendHotel() {
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));
            given(hotelRepository.save(any())).willReturn(activeHotel);

            hotelService.suspendHotel(managerId, hotelId);

            assertThat(activeHotel.getStatus()).isEqualTo(HotelStatus.SUSPENDED);
        }

        @Test
        @DisplayName("should throw UnauthorizedHotelAccessException when another manager tries to suspend")
        void shouldThrowForUnauthorizedManager() {
            UUID otherId = UUID.randomUUID();
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(activeHotel));

            assertThatThrownBy(() -> hotelService.suspendHotel(otherId, hotelId))
                    .isInstanceOf(UnauthorizedHotelAccessException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  getMyHotels
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getMyHotels()")
    class GetMyHotels {

        @Test
        @DisplayName("should return paginated hotels for the given manager")
        void shouldReturnManagerHotels() {
            given(hotelRepository.findAllByManager_Id(managerId, pageable))
                    .willReturn(new PageImpl<>(List.of(activeHotel)));
            given(hotelMapper.toHotelResponse(activeHotel)).willReturn(hotelResponse);

            Page<HotelResponse> result = hotelService.getMyHotels(managerId, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty page when manager has no hotels")
        void shouldReturnEmptyPageWhenNoHotels() {
            given(hotelRepository.findAllByManager_Id(managerId, pageable))
                    .willReturn(new PageImpl<>(List.of()));

            Page<HotelResponse> result = hotelService.getMyHotels(managerId, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }
}

