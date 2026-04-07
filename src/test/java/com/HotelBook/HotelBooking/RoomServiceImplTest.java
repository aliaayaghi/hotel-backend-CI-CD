package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.Room.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private UUID hotelId;
    private UUID roomId;
    private Room room;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        roomId  = UUID.randomUUID();

        room = Room.builder()
                .id(roomId)
                .hotelId(hotelId)
                .name("Deluxe King")
                .type(RoomType.DELUXE)
                .bedType(BedType.KING)
                .description("Spacious room")
                .maxAdults(2)
                .maxChildren(1)
                .quantity(5)
                .sizeSqm(new BigDecimal("45.00"))
                .floor(3)
                .view(RoomView.SEA)
                .price(new BigDecimal("250.00"))
                .isActive(true)
                .build();
    }

    // ── createRoom ─────────────────────────────────────────────────────────────

    @Test
    void createRoom_savesAndReturnsDTO() {
        RoomRequestDTO request = buildRequest("Deluxe King");
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
            Room r = inv.getArgument(0);
            r.setId(roomId);
            return r;
        });

        RoomResponseDTO result = roomService.createRoom(hotelId, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Deluxe King");
        assertThat(result.getHotelId()).isEqualTo(hotelId);
        assertThat(result.getIsActive()).isTrue();
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createRoom_defaultsViewToNoneWhenNull() {
        RoomRequestDTO request = buildRequest("Standard");
        request.setView(null);

        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> {
            Room r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        roomService.createRoom(hotelId, request);

        verify(roomRepository).save(argThat(r -> r.getView() == RoomView.NONE));
    }

    // ── getRoomById ────────────────────────────────────────────────────────────

    @Test
    void getRoomById_returnsDTO_whenFound() {
        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));

        RoomResponseDTO result = roomService.getRoomById(hotelId, roomId);

        assertThat(result.getId()).isEqualTo(roomId);
        assertThat(result.getName()).isEqualTo("Deluxe King");
    }

    @Test
    void getRoomById_throws_whenNotFound() {
        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(hotelId, roomId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(roomId.toString());
    }

    // ── getRoomsByHotel (active only) ──────────────────────────────────────────

    @Test
    void getRoomsByHotel_returnsOnlyActiveRooms() {
        when(roomRepository.findByHotelIdAndIsActiveTrue(hotelId)).thenReturn(List.of(room));

        List<RoomResponseDTO> result = roomService.getRoomsByHotel(hotelId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPhotos()).isEmpty();       // list DTO — no collections
        assertThat(result.get(0).getAmenities()).isEmpty();
    }

    @Test
    void getRoomsByHotel_returnsEmpty_whenNoActiveRooms() {
        when(roomRepository.findByHotelIdAndIsActiveTrue(hotelId)).thenReturn(List.of());

        assertThat(roomService.getRoomsByHotel(hotelId)).isEmpty();
    }

    // ── getAllRoomsByHotel ──────────────────────────────────────────────────────

    @Test
    void getAllRoomsByHotel_returnsAllRooms() {
        Room inactive = Room.builder().id(UUID.randomUUID()).hotelId(hotelId)
                .name("Old Suite").isActive(false).view(RoomView.NONE).build();
        when(roomRepository.findByHotelId(hotelId)).thenReturn(List.of(room, inactive));

        List<RoomResponseDTO> result = roomService.getAllRoomsByHotel(hotelId);

        assertThat(result).hasSize(2);
    }

    // ── updateRoom ─────────────────────────────────────────────────────────────

    @Test
    void updateRoom_updatesOnlyProvidedFields() {
        RoomRequestDTO partial = new RoomRequestDTO();
        partial.setName("Updated Name");
        partial.setPrice(new BigDecimal("300.00"));

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        RoomResponseDTO result = roomService.updateRoom(hotelId, roomId, partial);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPrice()).isEqualByComparingTo("300.00");
        assertThat(result.getType()).isEqualTo(RoomType.DELUXE);   // unchanged
    }

    @Test
    void updateRoom_throws_whenRoomNotFound() {
        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.updateRoom(hotelId, roomId, new RoomRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteRoom (soft) ──────────────────────────────────────────────────────

    @Test
    void deleteRoom_deactivatesRoom() {
        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        roomService.deleteRoom(hotelId, roomId);

        verify(roomRepository).save(argThat(r -> !r.getIsActive()));
    }

    @Test
    void deleteRoom_throws_whenRoomNotFound() {
        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.deleteRoom(hotelId, roomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── toResponseDTO — collection mapping ────────────────────────────────────

    @Test
    void getRoomById_returnsEmptyCollections_whenRoomHasNullCollections() {
        room.setPhotos(null);
        room.setAmenities(null);
        room.setAccessibilities(null);
        room.setPricingRules(null);
        room.setCancellationPolicies(null);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));

        RoomResponseDTO dto = roomService.getRoomById(hotelId, roomId);

        assertThat(dto.getPhotos()).isEmpty();
        assertThat(dto.getAmenities()).isEmpty();
        assertThat(dto.getAccessibilities()).isEmpty();
        assertThat(dto.getPricingRules()).isEmpty();
        assertThat(dto.getCancellationPolicies()).isEmpty();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private RoomRequestDTO buildRequest(String name) {
        RoomRequestDTO req = new RoomRequestDTO();
        req.setName(name);
        req.setType(RoomType.DELUXE);
        req.setBedType(BedType.KING);
        req.setDescription("Desc");
        req.setMaxAdults(2);
        req.setMaxChildren(1);
        req.setQuantity(5);
        req.setSizeSqm(new BigDecimal("45.00"));
        req.setFloor(3);
        req.setView(RoomView.SEA);
        req.setPrice(new BigDecimal("250.00"));
        return req;
    }
}
