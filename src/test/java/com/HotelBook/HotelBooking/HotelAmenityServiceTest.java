package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.Common.exception.HotelNotFoundException;
import com.HotelBook.HotelBooking.Common.exception.UnauthorizedHotelAccessException;
import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.HotelAmenity.*;
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
class HotelAmenityServiceTest {

    @Mock private HotelAmenityRepository amenityRepository;
    @Mock private HotelRepository hotelRepository;
    @Mock private HotelAmenityMapper amenityMapper;

    @InjectMocks
    private HotelAmenityServiceImpl amenityService;

    private UUID hotelId;
    private UUID managerId;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        managerId = UUID.randomUUID();

        // Setup a mock Hotel entity
        hotel = new Hotel();
        hotel.setId(hotelId);
    }

    @Test
    @DisplayName("addAmenity: Should successfully create and return a response DTO")
    void addAmenity_Success() {
        // Arrange
        CreateAmenityRequest request = new CreateAmenityRequest();
        request.setName("Rooftop Bar");
        request.setCategory(AmenityCategory.DINING);
        request.setIcon("glass-cocktail");

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.existsByIdAndManager_Id(hotelId, managerId)).thenReturn(true);
        when(amenityRepository.existsByHotelIdAndNameIgnoreCase(hotelId, "Rooftop Bar")).thenReturn(false);

        // Mock the repository save
        when(amenityRepository.save(any(HotelAmenity.class))).thenAnswer(invocation -> {
            HotelAmenity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID()); // Simulate DB assigning ID
            return saved;
        });

        // Mock the mapper
        HotelAmenityResponse mockResponse = HotelAmenityResponse.builder()
                .name("Rooftop Bar")
                .category(AmenityCategory.DINING)
                .build();
        when(amenityMapper.toResponse(any(HotelAmenity.class))).thenReturn(mockResponse);

        // Act
        HotelAmenityResponse result = amenityService.addAmenity(hotelId, managerId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Rooftop Bar", result.getName());
        verify(amenityRepository).save(any(HotelAmenity.class));
        verify(amenityMapper).toResponse(any(HotelAmenity.class));
    }

    @Test
    @DisplayName("removeAmenity: Should throw exception when amenity belongs to a different hotel")
    void removeAmenity_WrongHotelContext() {
        // Arrange
        UUID amenityId = UUID.randomUUID();
        Hotel differentHotel = new Hotel();
        differentHotel.setId(UUID.randomUUID());

        // Use the Lombok builder from your HotelAmenity class
        HotelAmenity amenity = HotelAmenity.builder()
                .id(amenityId)
                .hotel(differentHotel)
                .name("Gym")
                .build();

        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(amenity));

        // Act & Assert
        assertThrows(HotelNotFoundException.class, () ->
                amenityService.removeAmenity(hotelId, amenityId, managerId)
        );

        verify(amenityRepository, never()).delete(any());
    }

    @Test
    @DisplayName("addAmenity: Should throw UnauthorizedHotelAccessException if manager ownership check fails")
    void addAmenity_Unauthorized() {
        // Arrange
        CreateAmenityRequest request = new CreateAmenityRequest();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(hotelRepository.existsByIdAndManager_Id(hotelId, managerId)).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedHotelAccessException.class, () ->
                amenityService.addAmenity(hotelId, managerId, request)
        );
    }
}