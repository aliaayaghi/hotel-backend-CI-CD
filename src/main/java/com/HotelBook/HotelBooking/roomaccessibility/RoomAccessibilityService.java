package com.HotelBook.HotelBooking.roomaccessibility;

import com.HotelBook.HotelBooking.room.Room;
import com.HotelBook.HotelBooking.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomAccessibilityService {

    private final RoomAccessibilityRepository accessibilityRepository;
    private final RoomRepository roomRepository;


    @Transactional(readOnly = true)
    public List<RoomAccessibilityResponseDTO> getAccessibilitiesByRoom(UUID hotelId, UUID roomId) {
        verifyRoomOwnership(hotelId, roomId);
        return accessibilityRepository.findByRoomId(roomId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomAccessibilityResponseDTO addAccessibility(UUID hotelId, UUID roomId,
                                                         RoomAccessibilityRequestDTO request) {
        Room room = verifyRoomOwnership(hotelId, roomId);

        // Rule 2: prevent duplicate feature entries
        if (accessibilityRepository.existsByRoomIdAndFeatureIgnoreCase(roomId, request.getFeature())) {
            throw new RuntimeException(
                    "Feature '" + request.getFeature() + "' already exists for this room.");
        }

        RoomAccessibility feature = RoomAccessibility.builder()
                .room(room)
                .feature(request.getFeature())
                .isAvailable(request.getIsAvailable())
                .build();

        RoomAccessibility saved = accessibilityRepository.save(feature);
        log.info("Accessibility feature '{}' added to room {}", request.getFeature(), roomId);
        return toResponseDTO(saved);
    }


    @Transactional
    public RoomAccessibilityResponseDTO updateAccessibility(UUID hotelId, UUID roomId, UUID accessId,
                                                            RoomAccessibilityRequestDTO request) {
        verifyRoomOwnership(hotelId, roomId);

        RoomAccessibility feature = accessibilityRepository.findByIdAndRoomId(accessId, roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Accessibility feature not found with id: " + accessId + " for room: " + roomId));

        // If changing the feature name, check for duplicates
        if (request.getFeature() != null && !request.getFeature().equalsIgnoreCase(feature.getFeature())) {
            if (accessibilityRepository.existsByRoomIdAndFeatureIgnoreCase(roomId, request.getFeature())) {
                throw new RuntimeException(
                        "Feature '" + request.getFeature() + "' already exists for this room.");
            }
            feature.setFeature(request.getFeature());
        }

        // isAvailable can always be updated (true ↔ false)
        if (request.getIsAvailable() != null) {
            feature.setIsAvailable(request.getIsAvailable());
        }

        RoomAccessibility saved = accessibilityRepository.save(feature);
        log.info("Accessibility feature {} updated for room {}", accessId, roomId);
        return toResponseDTO(saved);
    }



    @Transactional
    public void deleteAccessibility(UUID hotelId, UUID roomId, UUID accessId) {
        verifyRoomOwnership(hotelId, roomId);

        RoomAccessibility feature = accessibilityRepository.findByIdAndRoomId(accessId, roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Accessibility feature not found with id: " + accessId + " for room: " + roomId));

        accessibilityRepository.delete(feature);
        log.info("Accessibility feature {} deleted from room {}", accessId, roomId);
    }



    private Room verifyRoomOwnership(UUID hotelId, UUID roomId) {
        return roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));
    }

    private RoomAccessibilityResponseDTO toResponseDTO(RoomAccessibility a) {
        RoomAccessibilityResponseDTO dto = new RoomAccessibilityResponseDTO();
        dto.setId(a.getId());
        dto.setRoomId(a.getRoom().getId());
        dto.setFeature(a.getFeature());
        dto.setIsAvailable(a.getIsAvailable());
        return dto;
    }
}
