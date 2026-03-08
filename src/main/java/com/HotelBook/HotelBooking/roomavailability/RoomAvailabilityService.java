package com.HotelBook.HotelBooking.roomavailability;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomAvailabilityService {

    private final RoomAvailabilityRepository availabilityRepository;


    @Transactional(readOnly = true)
    public boolean isAvailable(UUID roomId, LocalDate checkIn, LocalDate checkOut,
                               int requestedCount, int roomQuantity) {

        // Collect all the nights in the stay
        List<LocalDate> nights = checkIn.datesUntil(checkOut).collect(Collectors.toList());

        // Load all existing blocked records for these nights in ONE query (avoids N+1)
        List<RoomAvailability> blockedRecords =
                availabilityRepository.findByRoomIdAndDateIn(roomId, nights);

        // Build a map: date → blockedCount for fast lookup
        java.util.Map<LocalDate, Integer> blockedByDate = new java.util.HashMap<>();
        for (RoomAvailability record : blockedRecords) {
            blockedByDate.put(record.getDate(), record.getBlockedCount());
        }

        // Check each night
        for (LocalDate night : nights) {
            int blocked = blockedByDate.getOrDefault(night, 0);
            int available = roomQuantity - blocked;

            if (available < requestedCount) {
                log.debug("Room {} unavailable on {} — blocked={}, qty={}, requested={}",
                        roomId, night, blocked, roomQuantity, requestedCount);
                return false;
            }
        }

        return true;
    }

    @Transactional
    public void blockDates(UUID roomId, LocalDate checkIn, LocalDate checkOut,
                           int count, UUID bookingId) {

        checkIn.datesUntil(checkOut).forEach(date -> {
            availabilityRepository.findByRoomIdAndDate(roomId, date)
                    .ifPresentOrElse(
                            existing -> {
                                // Row exists → increment blocked count
                                existing.setBlockedCount(existing.getBlockedCount() + count);
                                availabilityRepository.save(existing);
                            },
                            () -> {
                                // No row → insert new blocked record
                                availabilityRepository.save(
                                        RoomAvailability.builder()
                                                .roomId(roomId)
                                                .date(date)
                                                .blockedCount(count)
                                                .blockedReason(RoomAvailability.BlockedReason.BOOKING)
                                                .bookingId(bookingId)
                                                .build()
                                );
                            }
                    );
        });

        log.info("Blocked dates {} to {} for room {} (booking: {})",
                checkIn, checkOut, roomId, bookingId);
    }


    @Transactional
    public void unblockDates(UUID roomId, LocalDate checkIn, LocalDate checkOut, int count) {
        checkIn.datesUntil(checkOut).forEach(date -> {
            availabilityRepository.findByRoomIdAndDate(roomId, date).ifPresent(existing -> {
                int newCount = existing.getBlockedCount() - count;
                if (newCount <= 0) {
                    // No more blocked rooms on this date → delete the row
                    availabilityRepository.delete(existing);
                } else {
                    // Still some rooms blocked → just decrement
                    existing.setBlockedCount(newCount);
                    availabilityRepository.save(existing);
                }
            });
        });

        log.info("Unblocked dates {} to {} for room {}", checkIn, checkOut, roomId);
    }

    @Transactional
    public void unblockByBookingId(UUID bookingId) {
        availabilityRepository.deleteByBookingId(bookingId);
        log.info("Unblocked all dates for booking {}", bookingId);
    }


    @Transactional
    public void manualBlock(UUID roomId, LocalDate from, LocalDate to,
                            RoomAvailability.BlockedReason reason) {

        from.datesUntil(to).forEach(date -> {
            availabilityRepository.findByRoomIdAndDate(roomId, date)
                    .ifPresentOrElse(
                            existing -> {
                                existing.setBlockedCount(existing.getBlockedCount() + 1);
                                availabilityRepository.save(existing);
                            },
                            () -> availabilityRepository.save(
                                    RoomAvailability.builder()
                                            .roomId(roomId)
                                            .date(date)
                                            .blockedCount(1)
                                            .blockedReason(reason)
                                            .build()
                            )
                    );
        });

        log.info("Manual block ({}) applied to room {} from {} to {}", reason, roomId, from, to);
    }


    @Transactional
    public void manualUnblock(UUID roomId, LocalDate from, LocalDate to) {
        from.datesUntil(to).forEach(date -> {
            availabilityRepository.findByRoomIdAndDate(roomId, date).ifPresent(existing -> {
                int newCount = existing.getBlockedCount() - 1;
                if (newCount <= 0) {
                    availabilityRepository.delete(existing);
                } else {
                    existing.setBlockedCount(newCount);
                    availabilityRepository.save(existing);
                }
            });
        });

        log.info("Manual unblock applied to room {} from {} to {}", roomId, from, to);
    }


    @Transactional(readOnly = true)
    public List<RoomAvailability> getBlockedDates(UUID roomId, LocalDate from, LocalDate to) {
        return availabilityRepository.findByRoomIdAndDateBetween(roomId, from, to);
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySummary> getAvailabilitySummary(UUID roomId, LocalDate from,
                                                            LocalDate to, int roomQuantity) {
        List<LocalDate> allDates = from.datesUntil(to).collect(Collectors.toList());
        List<RoomAvailability> blocked = availabilityRepository.findByRoomIdAndDateBetween(roomId, from, to);

        // Map date → blockedCount
        java.util.Map<LocalDate, RoomAvailability> blockedMap = new java.util.HashMap<>();
        for (RoomAvailability b : blocked) {
            blockedMap.put(b.getDate(), b);
        }

        return allDates.stream().map(date -> {
            RoomAvailability record = blockedMap.get(date);
            int blockedCount = record != null ? record.getBlockedCount() : 0;
            String reason = record != null ? record.getBlockedReason().name() : null;
            return new AvailabilitySummary(date, roomQuantity - blockedCount, blockedCount,
                    blockedCount >= roomQuantity, reason);
        }).collect(Collectors.toList());
    }

    // ── INNER SUMMARY CLASS ───────────────────────────────────────────────────

    /**
     * AvailabilitySummary — one entry per date in the calendar response.
     * Sent to the frontend so it can show availability colours per day.
     */
    public static class AvailabilitySummary {
        private final LocalDate date;
        private final int availableCount;
        private final int blockedCount;
        private final boolean fullyBooked;
        private final String blockedReason;  // null if no blocks

        public AvailabilitySummary(LocalDate date, int availableCount, int blockedCount,
                                   boolean fullyBooked, String blockedReason) {
            this.date = date;
            this.availableCount = availableCount;
            this.blockedCount = blockedCount;
            this.fullyBooked = fullyBooked;
            this.blockedReason = blockedReason;
        }

        public LocalDate getDate()         { return date; }
        public int getAvailableCount()     { return availableCount; }
        public int getBlockedCount()       { return blockedCount; }
        public boolean isFullyBooked()     { return fullyBooked; }
        public String getBlockedReason()   { return blockedReason; }
    }
}
