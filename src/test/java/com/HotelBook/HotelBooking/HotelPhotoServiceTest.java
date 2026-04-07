package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.Common.exception.UnauthorizedException;
import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.HotelPhoto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelPhotoServiceTest {

    @Mock private HotelPhotoRepository photoRepository;
    @Mock private HotelRepository hotelRepository;
    @Mock private HotelPhotoMapper photoMapper;

    @InjectMocks
    private HotelPhotoServiceImpl photoService;

    private UUID hotelId;
    private UUID managerId;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        hotel = new Hotel();
        hotel.setId(hotelId);
    }

    @Test
    @DisplayName("addPhoto: Should clear existing cover if new photo is set as cover")
    void addPhoto_SetsCover_ClearsExisting() {
        // Arrange
        CreatePhotoRequest request = new CreatePhotoRequest();
        request.setUrl("http://image.jpg");
        request.setCover(true);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.existsByIdAndManager_Id(hotelId, managerId)).thenReturn(true);
        when(photoRepository.save(any(HotelPhoto.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        photoService.addPhoto(hotelId, managerId, request);

        // Assert
        verify(photoRepository, times(1)).clearCoverByHotelId(hotelId);
        verify(photoRepository).save(any(HotelPhoto.class));
    }

    @Test
    @DisplayName("reorderPhotos: Should throw exception if count of IDs does not match existing photos")
    void reorderPhotos_SizeMismatch_ThrowsException() {
        // Arrange
        ReorderPhotosRequest request = new ReorderPhotosRequest();
        request.setPhotoIds(Arrays.asList(UUID.randomUUID())); // Only 1 ID provided

        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(hotelRepository.existsByIdAndManager_Id(hotelId, managerId)).thenReturn(true);

        // Mock 3 existing photos
        List<HotelPhoto> existing = Arrays.asList(new HotelPhoto(), new HotelPhoto(), new HotelPhoto());
        when(photoRepository.findByHotelId(hotelId)).thenReturn(existing);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                photoService.reorderPhotos(hotelId, managerId, request)
        );
    }

    @Test
    @DisplayName("deletePhoto: Should throw UnauthorizedException if manager doesn't own hotel")
    void deletePhoto_Unauthorized() {
        // Arrange
        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(hotelRepository.existsByIdAndManager_Id(hotelId, managerId)).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                photoService.deletePhoto(hotelId, UUID.randomUUID(), managerId)
        );
    }
}
