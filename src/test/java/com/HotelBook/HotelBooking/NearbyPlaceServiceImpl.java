package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Common.exception.HotelNotFoundException;
import com.HotelBook.HotelBooking.Common.exception.UnauthorizedHotelAccessException;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.HotelNearby.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NearbyPlaceServiceTest {

    @Mock private NearbyPlaceRepository nearbyPlaceRepository;
    @Mock private HotelRepository hotelRepository;
    @Mock private NearbyPlaceMapper nearbyPlaceMapper;

    @InjectMocks
    private NearbyPlaceServiceImpl nearbyPlaceService;

    private UUID hotelId;
    private UUID managerId;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        managerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("addNearbyPlace: Should successfully add place when manager is authorized")
    void addNearbyPlace_Success() {
        // Arrange
        CreateNearbyPlaceRequest request = new CreateNearbyPlaceRequest();
        request.setName("Central Park");
        request.setType(NearbyPlaceType.MALL);


        when(hotelRepository.existsById(hotelId)).thenReturn(true);
        when(hotelRepository.existsByIdAndManager_Id(hotelId, managerId)).thenReturn(true);
        when(nearbyPlaceRepository.save(any(NearbyPlace.class))).thenAnswer(i -> i.getArgument(0));


        // Act
        NearbyPlaceResponse response = nearbyPlaceService.addNearbyPlace(hotelId, managerId, request);

        // Assert
       // assertNotNull(response);
        verify(nearbyPlaceRepository).save(any(NearbyPlace.class));
    }

    @Test
    @DisplayName("removeNearbyPlace: Should throw exception if place belongs to another hotel")
    void removeNearbyPlace_WrongHotelContext() {
        // Arrange
        UUID placeId = UUID.randomUUID();
        UUID otherHotelId = UUID.randomUUID();

        NearbyPlace place = NearbyPlace.builder()
                .id(placeId)
                .hotelId(otherHotelId) // Different hotel!
                .name("Airport")
                .build();

        when(nearbyPlaceRepository.findById(placeId)).thenReturn(Optional.of(place));

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () ->
                nearbyPlaceService.removeNearbyPlace(hotelId, placeId, managerId)
        );

        verify(nearbyPlaceRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getNearbyPlaces: Should throw exception if hotel does not exist")
    void getNearbyPlaces_HotelNotFound() {
        // Arrange
        when(hotelRepository.existsById(hotelId)).thenReturn(false);

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () ->
                nearbyPlaceService.getNearbyPlaces(hotelId)
        );
    }
}