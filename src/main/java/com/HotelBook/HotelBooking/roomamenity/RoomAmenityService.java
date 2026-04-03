package com.HotelBook.HotelBooking.roomamenity;


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
public class RoomAmenityService {

    private final RoomAmenityRepository amenityRepository;
    private final RoomRepository roomRepository;



    @Transactional(readOnly = true)
    public List<RoomAmenityResponseDTO> getAmenitiesByRoom(UUID hotelId, UUID roomId) {
        verifyRoomOwnership(hotelId, roomId);
        return amenityRepository.findByRoomId(roomId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }



    @Transactional
    public RoomAmenityResponseDTO addAmenity(UUID hotelId, UUID roomId, RoomAmenityRequestDTO request) {
        Room room = verifyRoomOwnership(hotelId, roomId);


        if (amenityRepository.existsByRoomIdAndNameIgnoreCase(roomId, request.getName())) {
            throw new RuntimeException(
                    "Amenity '" + request.getName() + "' already exists for this room.");
        }

        RoomAmenity amenity = RoomAmenity.builder()
                .room(room)
                .name(request.getName())
                .category(request.getCategory())
                .icon(request.getIcon())
                .build();

        RoomAmenity saved = amenityRepository.save(amenity);
        log.info("Amenity '{}' added to room {}", request.getName(), roomId);
        return toResponseDTO(saved);
    }


    @Transactional
    public RoomAmenityResponseDTO updateAmenity(UUID hotelId, UUID roomId, UUID amenityId,
                                                RoomAmenityRequestDTO request) {
        verifyRoomOwnership(hotelId, roomId);

        RoomAmenity amenity = amenityRepository.findByIdAndRoomId(amenityId, roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Amenity not found with id: " + amenityId + " for room: " + roomId));


        if (request.getName() != null && !request.getName().equalsIgnoreCase(amenity.getName())) {
            if (amenityRepository.existsByRoomIdAndNameIgnoreCase(roomId, request.getName())) {
                throw new RuntimeException(
                        "Amenity '" + request.getName() + "' already exists for this room.");
            }
            amenity.setName(request.getName());
        }

        if (request.getCategory() != null) amenity.setCategory(request.getCategory());
        if (request.getIcon() != null)     amenity.setIcon(request.getIcon());

        RoomAmenity saved = amenityRepository.save(amenity);
        log.info("Amenity {} updated for room {}", amenityId, roomId);
        return toResponseDTO(saved);
    }



    @Transactional
    public void deleteAmenity(UUID hotelId, UUID roomId, UUID amenityId) {
        verifyRoomOwnership(hotelId, roomId);

        RoomAmenity amenity = amenityRepository.findByIdAndRoomId(amenityId, roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Amenity not found with id: " + amenityId + " for room: " + roomId));

        amenityRepository.delete(amenity);
        log.info("Amenity {} deleted from room {}", amenityId, roomId);
    }



    private Room verifyRoomOwnership(UUID hotelId, UUID roomId) {
        return roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));
    }

    private RoomAmenityResponseDTO toResponseDTO(RoomAmenity amenity) {
        RoomAmenityResponseDTO dto = new RoomAmenityResponseDTO();
        dto.setId(amenity.getId());
        dto.setRoomId(amenity.getRoom().getId());
        dto.setName(amenity.getName());
        dto.setCategory(amenity.getCategory().name()); // enum → String "TECH"
        dto.setIcon(amenity.getIcon());
        return dto;
    }
}
