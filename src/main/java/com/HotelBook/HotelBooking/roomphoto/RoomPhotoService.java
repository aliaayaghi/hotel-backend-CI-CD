package com.HotelBook.HotelBooking.roomphoto;


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
public class RoomPhotoService {


    private static final int MAX_PHOTOS = 20;

    private final RoomPhotoRepository photoRepository;
    private final RoomRepository roomRepository;


    @Transactional(readOnly = true)
    public List<RoomPhotoResponseDTO> getPhotosByRoom(UUID hotelId, UUID roomId) {

        verifyRoomOwnership(hotelId, roomId);
        return photoRepository.findByRoomIdOrderByDisplayOrderAsc(roomId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }




    @Transactional
    public RoomPhotoResponseDTO addPhoto(UUID hotelId, UUID roomId, RoomPhotoRequestDTO request) {
        Room room = verifyRoomOwnership(hotelId, roomId);


        if (photoRepository.countByRoomId(roomId) >= MAX_PHOTOS) {
            throw new RuntimeException(
                    "Cannot add photo: room already has the maximum of " + MAX_PHOTOS + " photos.");
        }


        if (photoRepository.existsByRoomIdAndUrl(roomId, request.getUrl())) {
            throw new RuntimeException(
                    "Cannot add photo: this URL is already associated with this room.");
        }


        int order;
        if (request.getDisplayOrder() != null) {
            order = request.getDisplayOrder();
        } else {

            order = photoRepository.findMaxDisplayOrderByRoomId(roomId) + 1;
        }

        RoomPhoto photo = RoomPhoto.builder()
                .room(room)
                .url(request.getUrl())
                .displayOrder(order)
                .caption(request.getCaption())
                .build();

        RoomPhoto saved = photoRepository.save(photo);
        log.info("Photo added to room {}: {}", roomId, request.getUrl());
        return toResponseDTO(saved);
    }




    @Transactional
    public void deletePhoto(UUID hotelId, UUID roomId, UUID photoId) {
        verifyRoomOwnership(hotelId, roomId);

        RoomPhoto photo = photoRepository.findByIdAndRoomId(photoId, roomId)
                .orElseThrow(() -> new RuntimeException(
                        "Photo not found with id: " + photoId + " for room: " + roomId));

        photoRepository.delete(photo);
        log.info("Photo {} deleted from room {}", photoId, roomId);
    }




    private Room verifyRoomOwnership(UUID hotelId, UUID roomId) {
        return roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));
    }


    private RoomPhotoResponseDTO toResponseDTO(RoomPhoto photo) {
        RoomPhotoResponseDTO dto = new RoomPhotoResponseDTO();
        dto.setId(photo.getId());
        dto.setRoomId(photo.getRoom().getId());
        dto.setUrl(photo.getUrl());
        dto.setDisplayOrder(photo.getDisplayOrder());
        dto.setCaption(photo.getCaption());
        dto.setCreatedAt(photo.getCreatedAt());
        return dto;
    }
}
