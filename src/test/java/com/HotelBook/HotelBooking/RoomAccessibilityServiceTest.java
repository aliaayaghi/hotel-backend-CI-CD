package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Room.Room;
import com.HotelBook.HotelBooking.Room.RoomRepository;
import com.HotelBook.HotelBooking.Room.RoomView;
import com.HotelBook.HotelBooking.RoomAccessibility.*;
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
class RoomAccessibilityServiceTest {

    @Mock private RoomAccessibilityRepository accessibilityRepository;
    @Mock private RoomRepository roomRepository;

    @InjectMocks private RoomAccessibilityService accessibilityService;

    private UUID hotelId;
    private UUID roomId;
    private Room room;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        roomId  = UUID.randomUUID();
        room    = Room.builder().id(roomId).hotelId(hotelId).view(RoomView.NONE).isActive(true).build();
    }

    // ── getAccessibilitiesByRoom ───────────────────────────────────────────────

    @Test
    void getAccessibilitiesByRoom_returnsMappedDTOs() {
        RoomAccessibility feature = feature("Wheelchair ramp", true);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.findByRoomId(roomId)).thenReturn(List.of(feature));

        List<RoomAccessibilityResponseDTO> result =
                accessibilityService.getAccessibilitiesByRoom(hotelId, roomId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFeature()).isEqualTo("Wheelchair ramp");
        assertThat(result.get(0).getIsAvailable()).isTrue();
    }

    @Test
    void getAccessibilitiesByRoom_throws_whenRoomNotOwnedByHotel() {
        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessibilityService.getAccessibilitiesByRoom(hotelId, roomId))
                .isInstanceOf(RuntimeException.class);
    }

    // ── addAccessibility ──────────────────────────────────────────────────────

    @Test
    void addAccessibility_savesSuccessfully() {
        RoomAccessibilityRequestDTO req = new RoomAccessibilityRequestDTO();
        req.setFeature("Grab bars");
        req.setIsAvailable(true);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.existsByRoomIdAndFeatureIgnoreCase(roomId, "Grab bars")).thenReturn(false);
        when(accessibilityRepository.save(any(RoomAccessibility.class))).thenAnswer(inv -> {
            RoomAccessibility a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        RoomAccessibilityResponseDTO result = accessibilityService.addAccessibility(hotelId, roomId, req);

        assertThat(result.getFeature()).isEqualTo("Grab bars");
        assertThat(result.getIsAvailable()).isTrue();
    }

    @Test
    void addAccessibility_throws_whenDuplicateFeature() {
        RoomAccessibilityRequestDTO req = new RoomAccessibilityRequestDTO();
        req.setFeature("Grab bars");

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.existsByRoomIdAndFeatureIgnoreCase(roomId, "Grab bars")).thenReturn(true);

        assertThatThrownBy(() -> accessibilityService.addAccessibility(hotelId, roomId, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Grab bars");
    }

    // ── updateAccessibility ────────────────────────────────────────────────────

    @Test
    void updateAccessibility_togglingAvailability() {
        UUID accessId = UUID.randomUUID();
        RoomAccessibility existing = feature("Elevator access", true);
        existing.setId(accessId);

        RoomAccessibilityRequestDTO req = new RoomAccessibilityRequestDTO();
        req.setIsAvailable(false);   // toggle off

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.findByIdAndRoomId(accessId, roomId)).thenReturn(Optional.of(existing));
        when(accessibilityRepository.save(any(RoomAccessibility.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomAccessibilityResponseDTO result =
                accessibilityService.updateAccessibility(hotelId, roomId, accessId, req);

        assertThat(result.getIsAvailable()).isFalse();
    }

    @Test
    void updateAccessibility_renamesFeature_whenNewNameIsUnique() {
        UUID accessId = UUID.randomUUID();
        RoomAccessibility existing = feature("Old Feature", false);
        existing.setId(accessId);

        RoomAccessibilityRequestDTO req = new RoomAccessibilityRequestDTO();
        req.setFeature("New Feature");

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.findByIdAndRoomId(accessId, roomId)).thenReturn(Optional.of(existing));
        when(accessibilityRepository.existsByRoomIdAndFeatureIgnoreCase(roomId, "New Feature")).thenReturn(false);
        when(accessibilityRepository.save(any(RoomAccessibility.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomAccessibilityResponseDTO result =
                accessibilityService.updateAccessibility(hotelId, roomId, accessId, req);

        assertThat(result.getFeature()).isEqualTo("New Feature");
    }

    @Test
    void updateAccessibility_throws_whenNewNameAlreadyTaken() {
        UUID accessId = UUID.randomUUID();
        RoomAccessibility existing = feature("Feature A", true);
        existing.setId(accessId);

        RoomAccessibilityRequestDTO req = new RoomAccessibilityRequestDTO();
        req.setFeature("Feature B");   // already exists on the room

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.findByIdAndRoomId(accessId, roomId)).thenReturn(Optional.of(existing));
        when(accessibilityRepository.existsByRoomIdAndFeatureIgnoreCase(roomId, "Feature B")).thenReturn(true);

        assertThatThrownBy(() -> accessibilityService.updateAccessibility(hotelId, roomId, accessId, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Feature B");
    }

    @Test
    void updateAccessibility_throws_whenFeatureNotFound() {
        UUID accessId = UUID.randomUUID();

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.findByIdAndRoomId(accessId, roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessibilityService.updateAccessibility(
                hotelId, roomId, accessId, new RoomAccessibilityRequestDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(accessId.toString());
    }

    // ── deleteAccessibility ────────────────────────────────────────────────────

    @Test
    void deleteAccessibility_deletesRecord() {
        UUID accessId = UUID.randomUUID();
        RoomAccessibility existing = feature("Braille signage", true);
        existing.setId(accessId);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.findByIdAndRoomId(accessId, roomId)).thenReturn(Optional.of(existing));

        accessibilityService.deleteAccessibility(hotelId, roomId, accessId);

        verify(accessibilityRepository).delete(existing);
    }

    @Test
    void deleteAccessibility_throws_whenFeatureNotFound() {
        UUID accessId = UUID.randomUUID();

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(accessibilityRepository.findByIdAndRoomId(accessId, roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessibilityService.deleteAccessibility(hotelId, roomId, accessId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(accessId.toString());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private RoomAccessibility feature(String name, boolean available) {
        RoomAccessibility a = new RoomAccessibility();
        a.setId(UUID.randomUUID());
        a.setRoom(room);
        a.setFeature(name);
        a.setIsAvailable(available);
        return a;
    }
}
