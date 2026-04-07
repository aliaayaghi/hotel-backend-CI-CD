package com.HotelBook.HotelBooking;

import com.HotelBook.HotelBooking.RoomAvailability.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceTest {

    @Mock private RoomAvailabilityRepository availabilityRepository;

    @InjectMocks private RoomAvailabilityService availabilityService;

    private UUID roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() {
        roomId   = UUID.randomUUID();
        checkIn  = LocalDate.of(2026, 6, 1);
        checkOut = LocalDate.of(2026, 6, 4);   // 3 nights
    }

    // ── isAvailable ────────────────────────────────────────────────────────────

    @Test
    void isAvailable_returnsTrue_whenNoDatesBlocked() {
        when(availabilityRepository.findByRoomIdAndDateIn(eq(roomId), any())).thenReturn(List.of());

        assertThat(availabilityService.isAvailable(roomId, checkIn, checkOut, 1, 5)).isTrue();
    }

    @Test
    void isAvailable_returnsTrue_whenBlockedCountBelowCapacity() {
        // 2 rooms blocked out of 5 — requesting 2 should still succeed (5 - 2 = 3 >= 2)
        RoomAvailability blocked = blocked(checkIn, 2);
        when(availabilityRepository.findByRoomIdAndDateIn(eq(roomId), any()))
                .thenReturn(List.of(blocked));

        assertThat(availabilityService.isAvailable(roomId, checkIn, checkOut, 2, 5)).isTrue();
    }

    @Test
    void isAvailable_returnsFalse_whenFullyBooked() {
        RoomAvailability full = blocked(checkIn, 5);
        when(availabilityRepository.findByRoomIdAndDateIn(eq(roomId), any()))
                .thenReturn(List.of(full));

        assertThat(availabilityService.isAvailable(roomId, checkIn, checkOut, 1, 5)).isFalse();
    }

    @Test
    void isAvailable_returnsFalse_whenSingleNightBlocked() {
        // Only the middle night is fully blocked
        LocalDate middleNight = checkIn.plusDays(1);
        RoomAvailability blocked = blocked(middleNight, 5);
        when(availabilityRepository.findByRoomIdAndDateIn(eq(roomId), any()))
                .thenReturn(List.of(blocked));

        assertThat(availabilityService.isAvailable(roomId, checkIn, checkOut, 1, 5)).isFalse();
    }

    // ── blockDates ─────────────────────────────────────────────────────────────

    @Test
    void blockDates_createsNewRecords_whenNoneExist() {
        UUID bookingId = UUID.randomUUID();

        when(availabilityRepository.findByRoomIdAndDate(eq(roomId), any()))
                .thenReturn(Optional.empty());
        when(availabilityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        availabilityService.blockDates(roomId, checkIn, checkOut, 1, bookingId);

        // 3 nights → 3 saves
        verify(availabilityRepository, times(3)).save(any(RoomAvailability.class));
    }

    @Test
    void blockDates_incrementsExistingRecord() {
        UUID bookingId = UUID.randomUUID();
        RoomAvailability existing = blocked(checkIn, 1);

        // First night already has a record; remaining nights don't
        when(availabilityRepository.findByRoomIdAndDate(roomId, checkIn))
                .thenReturn(Optional.of(existing));
        when(availabilityRepository.findByRoomIdAndDate(eq(roomId),
                argThat(d -> !d.equals(checkIn))))
                .thenReturn(Optional.empty());
        when(availabilityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        availabilityService.blockDates(roomId, checkIn, checkOut, 2, bookingId);

        // Existing record should have count incremented to 3 (1 + 2)
        assertThat(existing.getBlockedCount()).isEqualTo(3);
    }

    // ── unblockDates ───────────────────────────────────────────────────────────

    @Test
    void unblockDates_decrementsCount() {
        RoomAvailability existing = blocked(checkIn, 3);

        when(availabilityRepository.findByRoomIdAndDate(eq(roomId), any()))
                .thenReturn(Optional.of(existing));
        when(availabilityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        availabilityService.unblockDates(roomId, checkIn, checkIn.plusDays(1), 1);

        assertThat(existing.getBlockedCount()).isEqualTo(2);
        verify(availabilityRepository, never()).delete(any());
    }

    @Test
    void unblockDates_deletesRecord_whenCountDropsToZero() {
        RoomAvailability existing = blocked(checkIn, 1);

        when(availabilityRepository.findByRoomIdAndDate(eq(roomId), any()))
                .thenReturn(Optional.of(existing));

        availabilityService.unblockDates(roomId, checkIn, checkIn.plusDays(1), 1);

        verify(availabilityRepository).delete(existing);
        verify(availabilityRepository, never()).save(any());
    }

    // ── unblockByBookingId ─────────────────────────────────────────────────────

    @Test
    void unblockByBookingId_deletesAllDatesForBooking() {
        UUID bookingId = UUID.randomUUID();

        availabilityService.unblockByBookingId(bookingId);

        verify(availabilityRepository).deleteByBookingId(bookingId);
    }

    // ── manualBlock ────────────────────────────────────────────────────────────

    /**
     * The service validates dates BEFORE parsing the reason, so an invalid date range
     * must throw even when the reason is valid.
     * NOTE: "BOOKING" reason also throws, but the date validation fires first when
     * the range is invalid, so the message contains "toDate".
     */
    @Test
    void manualBlock_rejects_invalidDateRange() {
        RoomAvailabilityRequestDTO req = new RoomAvailabilityRequestDTO();
        req.setFromDate(checkOut);  // reversed: fromDate > toDate
        req.setToDate(checkIn);
        req.setReason("MAINTENANCE");

        assertThatThrownBy(() -> availabilityService.manualBlock(roomId, 5, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toDate");
    }

    @Test
    void manualBlock_rejects_whenBookingReasonProvided() {
        RoomAvailabilityRequestDTO req = new RoomAvailabilityRequestDTO();
        req.setFromDate(checkIn);
        req.setToDate(checkOut);
        req.setReason("BOOKING");

        assertThatThrownBy(() -> availabilityService.manualBlock(roomId, 5, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BOOKING");
    }

    @Test
    void manualBlock_rejects_unknownReason() {
        RoomAvailabilityRequestDTO req = new RoomAvailabilityRequestDTO();
        req.setFromDate(checkIn);
        req.setToDate(checkOut);
        req.setReason("NONSENSE");

        assertThatThrownBy(() -> availabilityService.manualBlock(roomId, 5, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NONSENSE");
    }

    @Test
    void manualBlock_defaultsToManagerBlock_whenReasonIsNull() {
        RoomAvailabilityRequestDTO req = new RoomAvailabilityRequestDTO();
        req.setFromDate(checkIn);
        req.setToDate(checkOut);
        req.setReason(null);

        when(availabilityRepository.findByRoomIdAndDate(eq(roomId), any()))
                .thenReturn(Optional.empty());
        when(availabilityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(availabilityRepository.findByRoomIdAndDateBetween(eq(roomId), any(), any()))
                .thenReturn(List.of());

        assertThatNoException().isThrownBy(() -> availabilityService.manualBlock(roomId, 5, req));

        verify(availabilityRepository, times(3)).save(
                argThat(a -> a.getBlockedReason() == RoomAvailability.BlockedReason.MANAGER_BLOCK));
    }

    // ── manualUnblock ──────────────────────────────────────────────────────────

    @Test
    void manualUnblock_rejects_invalidDateRange() {
        RoomAvailabilityRequestDTO req = new RoomAvailabilityRequestDTO();
        req.setFromDate(checkOut);  // reversed
        req.setToDate(checkIn);

        assertThatThrownBy(() -> availabilityService.manualUnblock(roomId, 5, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toDate");
    }

    // ── getAvailabilitySummary ─────────────────────────────────────────────────

    @Test
    void getAvailabilitySummary_correctlyCalculatesAvailableCount() {
        RoomAvailability blocked = blocked(checkIn, 2);
        when(availabilityRepository.findByRoomIdAndDateBetween(roomId, checkIn, checkOut))
                .thenReturn(List.of(blocked));

        List<AvailabilitySummaryResponseDTO> summary =
                availabilityService.getAvailabilitySummary(roomId, checkIn, checkOut, 5);

        assertThat(summary).hasSize(3);   // 3 nights

        AvailabilitySummaryResponseDTO firstNight = summary.stream()
                .filter(s -> s.getDate().equals(checkIn))
                .findFirst().orElseThrow();

        assertThat(firstNight.getAvailableCount()).isEqualTo(3);   // 5 - 2
        assertThat(firstNight.getBlockedCount()).isEqualTo(2);
    }

    @Test
    void getAvailabilitySummary_marksSoldOut_whenBlockedEqualsQuantity() {
        RoomAvailability full = blocked(checkIn, 5);
        when(availabilityRepository.findByRoomIdAndDateBetween(roomId, checkIn, checkOut))
                .thenReturn(List.of(full));

        List<AvailabilitySummaryResponseDTO> summary =
                availabilityService.getAvailabilitySummary(roomId, checkIn, checkOut, 5);

        AvailabilitySummaryResponseDTO firstNight = summary.stream()
                .filter(s -> s.getDate().equals(checkIn))
                .findFirst().orElseThrow();

        assertThat(firstNight.getAvailableCount()).isEqualTo(0);
        assertThat(firstNight.getFullyBooked()).isTrue();
    }

    @Test
    void getAvailabilitySummary_unblockedDatesHaveNullReason() {
        when(availabilityRepository.findByRoomIdAndDateBetween(roomId, checkIn, checkOut))
                .thenReturn(List.of());

        List<AvailabilitySummaryResponseDTO> summary =
                availabilityService.getAvailabilitySummary(roomId, checkIn, checkOut, 3);

        summary.forEach(s -> {
            assertThat(s.getBlockedCount()).isEqualTo(0);
            assertThat(s.getBlockedReason()).isNull();
            assertThat(s.getAvailableCount()).isEqualTo(3);
        });
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private RoomAvailability blocked(LocalDate date, int count) {
        return RoomAvailability.builder()
                .id(UUID.randomUUID())
                .roomId(roomId)
                .date(date)
                .blockedCount(count)
                .blockedReason(RoomAvailability.BlockedReason.BOOKING)
                .build();
    }
}
