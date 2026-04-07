package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Room.Room;
import com.HotelBook.HotelBooking.Room.RoomRepository;
import com.HotelBook.HotelBooking.Room.RoomView;
import com.HotelBook.HotelBooking.RoomAmenity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAmenityServiceTest {

    @Mock private RoomAmenityRepository amenityRepository;
    @Mock private RoomRepository roomRepository;

    @InjectMocks private RoomAmenityService amenityService;

    private UUID hotelId;
    private UUID roomId;
    private Room room;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        roomId  = UUID.randomUUID();
        room    = Room.builder().id(roomId).hotelId(hotelId).view(RoomView.NONE).isActive(true).build();
    }

    // ── getAmenitiesByRoom ─────────────────────────────────────────────────────

    @Test
    void getAmenitiesByRoom_returnsMappedList() {
        RoomAmenity a = amenity("Wi-Fi", AmenityCategory.TECH);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(amenityRepository.findByRoomId(roomId)).thenReturn(List.of(a));

        List<RoomAmenityResponseDTO> result = amenityService.getAmenitiesByRoom(hotelId, roomId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Wi-Fi");
        assertThat(result.get(0).getCategory()).isEqualTo("TECH");
    }

    @Test
    void getAmenitiesByRoom_throws_whenRoomNotOwnedByHotel() {
        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> amenityService.getAmenitiesByRoom(hotelId, roomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(roomId.toString());
    }

    // ── addAmenity ─────────────────────────────────────────────────────────────

    @Test
    void addAmenity_savesAndReturnsDTO() {
        RoomAmenityRequestDTO req = new RoomAmenityRequestDTO();
        req.setName("Air Conditioning");
        req.setCategory(AmenityCategory.COMFORT);
        req.setIcon("ac-icon");

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(amenityRepository.existsByRoomIdAndNameIgnoreCase(roomId, req.getName())).thenReturn(false);
        when(amenityRepository.save(any(RoomAmenity.class))).thenAnswer(inv -> {
            RoomAmenity a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        RoomAmenityResponseDTO result = amenityService.addAmenity(hotelId, roomId, req);

        assertThat(result.getName()).isEqualTo("Air Conditioning");
        assertThat(result.getCategory()).isEqualTo("COMFORT");
    }

    @Test
    void addAmenity_throws_whenNameAlreadyExists() {
        RoomAmenityRequestDTO req = new RoomAmenityRequestDTO();
        req.setName("Wi-Fi");

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(amenityRepository.existsByRoomIdAndNameIgnoreCase(roomId, "Wi-Fi")).thenReturn(true);

        assertThatThrownBy(() -> amenityService.addAmenity(hotelId, roomId, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Wi-Fi");
    }

    // ── updateAmenity ──────────────────────────────────────────────────────────

    @Test
    void updateAmenity_updatesNameAndCategory() {
        UUID amenityId = UUID.randomUUID();
        RoomAmenity existing = amenity("Old Name", AmenityCategory.TECH);
        existing.setId(amenityId);

        RoomAmenityRequestDTO req = new RoomAmenityRequestDTO();
        req.setName("New Name");
        req.setCategory(AmenityCategory.BATHROOM);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(amenityRepository.findByIdAndRoomId(amenityId, roomId)).thenReturn(Optional.of(existing));
        when(amenityRepository.existsByRoomIdAndNameIgnoreCase(roomId, "New Name")).thenReturn(false);
        when(amenityRepository.save(any(RoomAmenity.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomAmenityResponseDTO result = amenityService.updateAmenity(hotelId, roomId, amenityId, req);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCategory()).isEqualTo("BATHROOM");
    }

    @Test
    void updateAmenity_allowsSameNameWithoutDuplicateCheck() {
        UUID amenityId = UUID.randomUUID();
        RoomAmenity existing = amenity("Wi-Fi", AmenityCategory.TECH);
        existing.setId(amenityId);

        RoomAmenityRequestDTO req = new RoomAmenityRequestDTO();
        req.setName("wi-fi");   // same name, different case — should NOT throw

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(amenityRepository.findByIdAndRoomId(amenityId, roomId)).thenReturn(Optional.of(existing));
        when(amenityRepository.save(any(RoomAmenity.class))).thenAnswer(inv -> inv.getArgument(0));

        // The service skips the duplicate check when the new name matches the existing one (case-insensitive).
        // Therefore, we must NOT stub existsByRoomIdAndNameIgnoreCase — Mockito strict-stub mode
        // would fail if we stub a method that is never called.
        assertThatNoException().isThrownBy(
                () -> amenityService.updateAmenity(hotelId, roomId, amenityId, req));
    }

    @Test
    void updateAmenity_throws_whenAmenityNotFound() {
        UUID amenityId = UUID.randomUUID();

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(amenityRepository.findByIdAndRoomId(amenityId, roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> amenityService.updateAmenity(hotelId, roomId, amenityId, new RoomAmenityRequestDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(amenityId.toString());
    }

    // ── deleteAmenity ──────────────────────────────────────────────────────────

    @Test
    void deleteAmenity_deletesRecord() {
        UUID amenityId = UUID.randomUUID();
        RoomAmenity existing = amenity("Minibar", AmenityCategory.TECH);
        existing.setId(amenityId);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(amenityRepository.findByIdAndRoomId(amenityId, roomId)).thenReturn(Optional.of(existing));

        amenityService.deleteAmenity(hotelId, roomId, amenityId);

        verify(amenityRepository).delete(existing);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private RoomAmenity amenity(String name, AmenityCategory category) {
        RoomAmenity a = new RoomAmenity();
        a.setId(UUID.randomUUID());
        a.setRoom(room);
        a.setName(name);
        a.setCategory(category);
        a.setIcon(null);
        return a;
    }
}
