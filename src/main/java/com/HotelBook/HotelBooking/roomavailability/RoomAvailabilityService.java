package com.HotelBook.HotelBooking.roomavailability;





import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        List<LocalDate> nights = checkIn.datesUntil(checkOut).collect(Collectors.toList());


        List<RoomAvailability> blockedRecords =
                availabilityRepository.findByRoomIdAndDateIn(roomId, nights);


        Map<LocalDate, Integer> blockedByDate = new HashMap<>();
        for (RoomAvailability record : blockedRecords) {
            blockedByDate.put(record.getDate(), record.getBlockedCount());
        }

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

        checkIn.datesUntil(checkOut).forEach(date ->
                availabilityRepository.findByRoomIdAndDate(roomId, date).ifPresentOrElse(
                        existing -> {
                            existing.setBlockedCount(existing.getBlockedCount() + count);
                            availabilityRepository.save(existing);
                        },
                        () -> availabilityRepository.save(
                                RoomAvailability.builder()
                                        .roomId(roomId)
                                        .date(date)
                                        .blockedCount(count)
                                        .blockedReason(RoomAvailability.BlockedReason.BOOKING)
                                        .bookingId(bookingId)
                                        .build()
                        )
                )
        );

        log.info("Blocked {} to {} for room {} (booking {})", checkIn, checkOut, roomId, bookingId);
    }


    @Transactional
    public void unblockDates(UUID roomId, LocalDate checkIn, LocalDate checkOut, int count) {
        checkIn.datesUntil(checkOut).forEach(date ->
                availabilityRepository.findByRoomIdAndDate(roomId, date).ifPresent(existing -> {
                    int newCount = existing.getBlockedCount() - count;
                    if (newCount <= 0) {
                        availabilityRepository.delete(existing);
                    } else {
                        existing.setBlockedCount(newCount);
                        availabilityRepository.save(existing);
                    }
                })
        );
        log.info("Unblocked {} to {} for room {}", checkIn, checkOut, roomId);
    }


    @Transactional
    public void unblockByBookingId(UUID bookingId) {
        availabilityRepository.deleteByBookingId(bookingId);
        log.info("Unblocked all dates for booking {}", bookingId);
    }


    @Transactional
    public List<AvailabilitySummaryResponseDTO> manualBlock(UUID roomId, int roomQuantity,
                                                            RoomAvailabilityRequestDTO request) {
        if (!request.getToDate().isAfter(request.getFromDate())) {
            throw new IllegalArgumentException("toDate must be after fromDate.");
        }

        RoomAvailability.BlockedReason reason = parseReason(request.getReason());

        // Reject if caller tries to set BOOKING reason manually (system-only)
        if (reason == RoomAvailability.BlockedReason.BOOKING) {
            throw new IllegalArgumentException(
                    "BOOKING reason is set automatically by the system. Use MAINTENANCE or MANAGER_BLOCK.");
        }

        request.getFromDate().datesUntil(request.getToDate()).forEach(date ->
                availabilityRepository.findByRoomIdAndDate(roomId, date).ifPresentOrElse(
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
                )
        );

        log.info("Manual block ({}) applied to room {} from {} to {}",
                reason, roomId, request.getFromDate(), request.getToDate());

        // Return the updated state for the blocked range
        return getAvailabilitySummary(roomId, request.getFromDate(), request.getToDate(), roomQuantity);
    }

    @Transactional
    public List<AvailabilitySummaryResponseDTO> manualUnblock(UUID roomId, int roomQuantity,
                                                              RoomAvailabilityRequestDTO request) {
        if (!request.getToDate().isAfter(request.getFromDate())) {
            throw new IllegalArgumentException("toDate must be after fromDate.");
        }

        request.getFromDate().datesUntil(request.getToDate()).forEach(date ->
                availabilityRepository.findByRoomIdAndDate(roomId, date).ifPresent(existing -> {
                    int newCount = existing.getBlockedCount() - 1;
                    if (newCount <= 0) {
                        availabilityRepository.delete(existing);
                    } else {
                        existing.setBlockedCount(newCount);
                        availabilityRepository.save(existing);
                    }
                })
        );

        log.info("Manual unblock applied to room {} from {} to {}",
                roomId, request.getFromDate(), request.getToDate());

        return getAvailabilitySummary(roomId, request.getFromDate(), request.getToDate(), roomQuantity);
    }


    @Transactional(readOnly = true)
    public List<AvailabilitySummaryResponseDTO> getAvailabilitySummary(UUID roomId,
                                                                       LocalDate from,
                                                                       LocalDate to,
                                                                       int roomQuantity) {
        List<LocalDate> allDates = from.datesUntil(to).collect(Collectors.toList());
        List<RoomAvailability> blocked = availabilityRepository.findByRoomIdAndDateBetween(roomId, from, to);

        Map<LocalDate, RoomAvailability> blockedMap = new HashMap<>();
        blocked.forEach(b -> blockedMap.put(b.getDate(), b));

        return allDates.stream().map(date -> {
            RoomAvailability record = blockedMap.get(date);
            int blockedCount = record != null ? record.getBlockedCount() : 0;
            String reason    = record != null ? record.getBlockedReason().name() : null;
            return new AvailabilitySummaryResponseDTO(
                    date,
                    roomQuantity - blockedCount,
                    blockedCount,
                    blockedCount >= roomQuantity,
                    reason
            );
        }).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<RoomAvailabilityResponseDTO> getBlockedDates(UUID roomId,
                                                             LocalDate from,
                                                             LocalDate to) {
        return availabilityRepository.findByRoomIdAndDateBetween(roomId, from, to)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    private RoomAvailability.BlockedReason parseReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return RoomAvailability.BlockedReason.MANAGER_BLOCK;
        }
        try {
            return RoomAvailability.BlockedReason.valueOf(reason.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid reason: '" + reason + "'. Must be MAINTENANCE or MANAGER_BLOCK.");
        }
    }

    /** Maps RoomAvailability entity → RoomAvailabilityResponseDTO. */
    private RoomAvailabilityResponseDTO toResponseDTO(RoomAvailability entity) {
        RoomAvailabilityResponseDTO dto = new RoomAvailabilityResponseDTO();
        dto.setId(entity.getId());
        dto.setRoomId(entity.getRoomId());
        dto.setDate(entity.getDate());
        dto.setBlockedCount(entity.getBlockedCount());
        dto.setBlockedReason(entity.getBlockedReason().name());
        dto.setBookingId(entity.getBookingId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}