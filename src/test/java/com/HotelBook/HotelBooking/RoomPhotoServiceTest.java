package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Room.Room;
import com.HotelBook.HotelBooking.Room.RoomRepository;
import com.HotelBook.HotelBooking.Room.RoomView;
import com.HotelBook.HotelBooking.RoomPhoto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomPhotoServiceTest {

    @Mock private RoomPhotoRepository photoRepository;
    @Mock private RoomRepository roomRepository;

    @InjectMocks private RoomPhotoService photoService;

    private UUID hotelId;
    private UUID roomId;
    private Room room;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        roomId  = UUID.randomUUID();
        room    = Room.builder().id(roomId).hotelId(hotelId).view(RoomView.NONE).isActive(true).build();
    }

    // ── getPhotosByRoom ────────────────────────────────────────────────────────

    @Test
    void getPhotosByRoom_returnsOrderedList() {
        RoomPhoto p1 = photo(1, "https://example.com/a.jpg");
        RoomPhoto p2 = photo(2, "https://example.com/b.jpg");

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(photoRepository.findByRoomIdOrderByDisplayOrderAsc(roomId)).thenReturn(List.of(p1, p2));

        List<RoomPhotoResponseDTO> result = photoService.getPhotosByRoom(hotelId, roomId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(result.get(1).getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void getPhotosByRoom_throws_whenRoomNotFound() {
        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> photoService.getPhotosByRoom(hotelId, roomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(roomId.toString());
    }

    // ── addPhoto ───────────────────────────────────────────────────────────────

    @Test
    void addPhoto_savesWithProvidedDisplayOrder() {
        RoomPhotoRequestDTO req = new RoomPhotoRequestDTO();
        req.setUrl("https://example.com/new.jpg");
        req.setDisplayOrder(5);
        req.setCaption("Lobby view");

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(photoRepository.countByRoomId(roomId)).thenReturn(3L);
        when(photoRepository.existsByRoomIdAndUrl(roomId, req.getUrl())).thenReturn(false);
        when(photoRepository.save(any(RoomPhoto.class))).thenAnswer(inv -> {
            RoomPhoto p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            p.setCreatedAt(LocalDateTime.now());
            return p;
        });

        RoomPhotoResponseDTO result = photoService.addPhoto(hotelId, roomId, req);

        assertThat(result.getUrl()).isEqualTo("https://example.com/new.jpg");
        assertThat(result.getDisplayOrder()).isEqualTo(5);
        assertThat(result.getCaption()).isEqualTo("Lobby view");
    }

    @Test
    void addPhoto_autoAssignsNextOrder_whenOrderNotProvided() {
        RoomPhotoRequestDTO req = new RoomPhotoRequestDTO();
        req.setUrl("https://example.com/auto.jpg");
        req.setDisplayOrder(null);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(photoRepository.countByRoomId(roomId)).thenReturn(2L);
        when(photoRepository.existsByRoomIdAndUrl(roomId, req.getUrl())).thenReturn(false);
        // findMaxDisplayOrderByRoomId returns int (COALESCE MAX, defaults to -1 when empty)
        when(photoRepository.findMaxDisplayOrderByRoomId(roomId)).thenReturn(4);
        when(photoRepository.save(any(RoomPhoto.class))).thenAnswer(inv -> {
            RoomPhoto p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            p.setCreatedAt(LocalDateTime.now());
            return p;
        });

        RoomPhotoResponseDTO result = photoService.addPhoto(hotelId, roomId, req);

        assertThat(result.getDisplayOrder()).isEqualTo(5);  // max(4) + 1
    }

    @Test
    void addPhoto_throws_whenMaxPhotosReached() {
        RoomPhotoRequestDTO req = new RoomPhotoRequestDTO();
        req.setUrl("https://example.com/overflow.jpg");

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(photoRepository.countByRoomId(roomId)).thenReturn(20L);   // at limit

        assertThatThrownBy(() -> photoService.addPhoto(hotelId, roomId, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("maximum of 20");
    }

    @Test
    void addPhoto_throws_whenDuplicateUrl() {
        RoomPhotoRequestDTO req = new RoomPhotoRequestDTO();
        req.setUrl("https://example.com/dup.jpg");

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(photoRepository.countByRoomId(roomId)).thenReturn(5L);
        when(photoRepository.existsByRoomIdAndUrl(roomId, req.getUrl())).thenReturn(true);

        assertThatThrownBy(() -> photoService.addPhoto(hotelId, roomId, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already associated");
    }

    // ── deletePhoto ────────────────────────────────────────────────────────────

    @Test
    void deletePhoto_deletesExistingPhoto() {
        UUID photoId = UUID.randomUUID();
        RoomPhoto p = photo(1, "https://example.com/del.jpg");
        p.setId(photoId);

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(photoRepository.findByIdAndRoomId(photoId, roomId)).thenReturn(Optional.of(p));

        photoService.deletePhoto(hotelId, roomId, photoId);

        verify(photoRepository).delete(p);
    }

    @Test
    void deletePhoto_throws_whenPhotoNotFound() {
        UUID photoId = UUID.randomUUID();

        when(roomRepository.findByIdAndHotelId(roomId, hotelId)).thenReturn(Optional.of(room));
        when(photoRepository.findByIdAndRoomId(photoId, roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> photoService.deletePhoto(hotelId, roomId, photoId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(photoId.toString());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private RoomPhoto photo(int order, String url) {
        RoomPhoto p = new RoomPhoto();
        p.setId(UUID.randomUUID());
        p.setRoom(room);
        p.setUrl(url);
        p.setDisplayOrder(order);
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }
}
