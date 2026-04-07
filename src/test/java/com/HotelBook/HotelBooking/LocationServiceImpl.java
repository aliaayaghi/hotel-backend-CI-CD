package com.HotelBook.HotelBooking;


import com.HotelBook.HotelBooking.Common.exception.LocationAlreadyExistsException;
import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.HotelLocation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock private LocationRepository locationRepository;
    @Mock private HotelRepository hotelRepository;
    @Mock private LocationMapper locationMapper;

    @InjectMocks
    private LocationServiceImpl locationService;

    private UUID hotelId;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        hotel = new Hotel();
        hotel.setId(hotelId);
    }

    @Test
    @DisplayName("createLocation: Should throw exception if hotel already has a location")
    void createLocation_AlreadyExists() {
        // Arrange
        CreateLocationRequest request = new CreateLocationRequest();
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(locationRepository.existsByHotel_Id(hotelId)).thenReturn(true);

        // Act & Assert
        assertThrows(LocationAlreadyExistsException.class, () ->
                locationService.createLocation(hotelId, request)
        );
    }

    @Test
    @DisplayName("findNearby: Should filter candidates correctly using Haversine formula")
    void findNearby_HaversineFiltering() {
        // Arrange: Center point (London)
        double centerLat = 51.5074;
        double centerLng = -0.1278;
        double radius = 10.0; // 10km

        // Close location (5km away)
        Location closeLoc = Location.builder().latitude(51.5000).longitude(-0.1000).build();
        // Far location (20km away, but might be in the bounding box)
        Location farLoc = Location.builder().latitude(51.7000).longitude(-0.3000).build();

        when(locationRepository.findWithinBoundingBox(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(closeLoc, farLoc));



        // Act
        List<NearbyHotelResponse> results = locationService.findNearby(centerLat, centerLng, radius);

        // Assert: Only the close location should remain after the Haversine filter
        assertEquals(1, results.size());
        verify(locationMapper, times(1)).toNearbyHotelResponse(eq(closeLoc), anyDouble());
    }

    @Test
    @DisplayName("updateLocation: Should perform a partial patch update")
    void updateLocation_PartialUpdate() {
        // Arrange
        UpdateLocationRequest request = new UpdateLocationRequest();
        request.setCity("New City"); // Only updating city

        Location existingLoc = Location.builder()
                .hotel(hotel)
                .city("Old City")
                .country("Old Country")
                .build();

        when(locationRepository.findByHotel_Id(hotelId)).thenReturn(Optional.of(existingLoc));
        when(locationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        locationService.updateLocation(hotelId, request);

        // Assert
        assertEquals("New City", existingLoc.getCity());
        assertEquals("Old Country", existingLoc.getCountry()); // Unchanged
        verify(locationRepository).save(existingLoc);
    }
}
