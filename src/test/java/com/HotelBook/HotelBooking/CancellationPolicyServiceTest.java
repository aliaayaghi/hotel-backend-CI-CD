package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Cancellation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancellationPolicyServiceTest {

    @Mock
    private CancellationPolicyRepository policyRepository;

    @InjectMocks
    private CancellationPolicyService policyService;

    private UUID hotelId;
    private UUID roomId;
    private CancellationPolicyRequestDTO request;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        request = new CancellationPolicyRequestDTO();
        request.setTierName("Flexible");
        request.setDeadlineHours(24);
        request.setRefundPercentage(100);
        request.setPriceMultiplier(new BigDecimal("1.10"));
        request.setIsDefault(true);
    }

    @Test
    @DisplayName("Should fallback to hotel policies if room policies are empty")
    void getPoliciesForRoom_FallbackToHotel() {
        // Arrange
        when(policyRepository.findByRoomId(roomId)).thenReturn(Collections.emptyList());

        CancellationPolicy hotelPolicy = CancellationPolicy.builder()
                .hotelId(hotelId).tierName("Hotel Wide").build();
        when(policyRepository.findByHotelIdAndRoomIdIsNull(hotelId))
                .thenReturn(List.of(hotelPolicy));

        // Act
        List<CancellationPolicyResponseDTO> result = policyService.getPoliciesForRoom(hotelId, roomId);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Hotel Wide", result.get(0).getTierName());
        verify(policyRepository).findByRoomId(roomId);
        verify(policyRepository).findByHotelIdAndRoomIdIsNull(hotelId);
    }

    @Test
    @DisplayName("Should throw exception when creating 6th tier for a room")
    void createRoomPolicy_LimitExceeded() {
        // Arrange
        when(policyRepository.countByRoomId(roomId)).thenReturn(5L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                policyService.createRoomPolicy(hotelId, roomId, request)
        );
        assertTrue(exception.getMessage().contains("maximum of 5"));
    }

    @Test
    @DisplayName("Should throw exception for inconsistent non-refundable settings")
    void validateConsistency_InvalidRequest() {
        // Arrange: 0 hours but claiming a refund
        request.setDeadlineHours(0);
        request.setRefundPercentage(50);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                policyService.createRoomPolicy(hotelId, roomId, request)
        );
    }

    @Test
    @DisplayName("Should clear existing defaults when a new default is created")
    void createRoomPolicy_ClearsOldDefault() {
        // Arrange
        when(policyRepository.countByRoomId(roomId)).thenReturn(0L);
        when(policyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        policyService.createRoomPolicy(hotelId, roomId, request);

        // Assert
        verify(policyRepository, times(1)).clearDefaultForRoom(roomId);
        verify(policyRepository).save(any());
    }
}
