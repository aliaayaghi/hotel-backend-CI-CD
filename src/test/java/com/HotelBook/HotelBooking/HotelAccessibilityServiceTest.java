package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.HotelAccessibility.*;
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
class HotelAccessibilityServiceTest {

    @Mock private HotelAccessibilityRepository accessibilityRepository;
    @Mock private HotelRepository hotelRepository;

    @InjectMocks
    private HotelAccessibilityServiceImpl accessibilityService;

    private UUID hotelId;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        hotel = new Hotel();
        hotel.setId(hotelId);
    }

    @Test
    @DisplayName("Should successfully add a feature to a valid hotel")
    void addFeature_Success() {
        // Arrange
        CreateAccessibilityRequest request = new CreateAccessibilityRequest();
        request.setFeature("Wheelchair Access");

        // FIX: Use the Enum constant instead of a String
        request.setLevel(AccessibilityLevel.NONE);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));

        // Mocking the save to return an entity with an ID
        when(accessibilityRepository.save(any(HotelAccessibility.class))).thenAnswer(i -> {
            HotelAccessibility saved = i.getArgument(0);
            return HotelAccessibility.builder()
                    .id(UUID.randomUUID())
                    .hotel(saved.getHotel())
                    .feature(saved.getFeature())
                    .level(saved.getLevel())
                    .description(saved.getDescription())
                    .build();
        });

        // Act
        HotelAccessibilityResponse response = accessibilityService.addFeature(hotelId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Wheelchair Access", response.getFeature());
        assertEquals(AccessibilityLevel.NONE, response.getLevel());
        verify(accessibilityRepository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when removing a feature that doesn't belong to the hotel")
    void removeFeature_WrongHotel_ThrowsException() {
        // Arrange
        UUID featureId = UUID.randomUUID();
        UUID differentHotelId = UUID.randomUUID();

        Hotel otherHotel = new Hotel();
        otherHotel.setId(differentHotelId);

        HotelAccessibility feature = HotelAccessibility.builder()
                .id(featureId)
                .hotel(otherHotel) // Feature belongs to different hotel
                .build();

        when(accessibilityRepository.findById(featureId)).thenReturn(Optional.of(feature));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                accessibilityService.removeFeature(hotelId, featureId)
        );

        verify(accessibilityRepository, never()).delete(any());
    }
}