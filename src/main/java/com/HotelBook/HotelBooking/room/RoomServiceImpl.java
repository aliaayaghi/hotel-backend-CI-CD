package com.HotelBook.HotelBooking.room;


import com.hotelapp.room.dto.RoomRequestDTO;
import com.hotelapp.room.dto.RoomResponseDTO;
import com.hotelapp.roomaccessibility.RoomAccessibility;
import com.hotelapp.roomamenity.RoomAmenity;
import com.hotelapp.roomphoto.RoomPhoto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;



    @Override
    @Transactional
    public RoomResponseDTO createRoom(UUID hotelId, RoomRequestDTO request) {
        Room room = Room.builder()
                .hotelId(hotelId)
                .name(request.getName())
                .type(request.getType())
                .bedType(request.getBedType())
                .description(request.getDescription())
                .maxAdults(request.getMaxAdults())
                .maxChildren(request.getMaxChildren())
                .quantity(request.getQuantity())
                .sizeSqm(request.getSizeSqm())
                .floor(request.getFloor())
                .view(request.getView() != null ? request.getView() : RoomView.NONE)
                .price(request.getPrice())
                .isActive(true)
                .build();

        Room saved = roomRepository.save(room);
        log.info("Room '{}' created for hotel {} with id {}", saved.getName(), hotelId, saved.getId());
        return toResponseDTO(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(UUID hotelId, UUID roomId) {
        Room room = roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));
        return toResponseDTO(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByHotel(UUID hotelId) {
        return roomRepository.findByHotelIdAndIsActiveTrue(hotelId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllRoomsByHotel(UUID hotelId) {
        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }




    @Override
    @Transactional
    public RoomResponseDTO updateRoom(UUID hotelId, UUID roomId, RoomRequestDTO request) {
        Room room = roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));

        if (request.getName() != null)        room.setName(request.getName());
        if (request.getType() != null)        room.setType(request.getType());
        if (request.getBedType() != null)     room.setBedType(request.getBedType());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getMaxAdults() != null)   room.setMaxAdults(request.getMaxAdults());
        if (request.getMaxChildren() != null) room.setMaxChildren(request.getMaxChildren());
        if (request.getQuantity() != null)    room.setQuantity(request.getQuantity());
        if (request.getSizeSqm() != null)     room.setSizeSqm(request.getSizeSqm());
        if (request.getFloor() != null)       room.setFloor(request.getFloor());
        if (request.getView() != null)        room.setView(request.getView());
        if (request.getPrice() != null)       room.setPrice(request.getPrice());

        Room saved = roomRepository.save(room);
        log.info("Room {} updated", roomId);
        return toResponseDTO(saved);
    }



    @Override
    @Transactional
    public void deleteRoom(UUID hotelId, UUID roomId) {
        Room room = roomRepository.findByIdAndHotelId(roomId, hotelId)
                .orElseThrow(() -> new RuntimeException(
                        "Room not found with id: " + roomId + " for hotel: " + hotelId));
        room.deactivate();
        roomRepository.save(room);
        log.info("Room {} soft-deleted (deactivated)", roomId);
    }




    private RoomResponseDTO toResponseDTO(Room room) {
        if (room == null) return null;

        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setHotelId(room.getHotelId());
        dto.setName(room.getName());
        dto.setType(room.getType());
        dto.setBedType(room.getBedType());
        dto.setDescription(room.getDescription());
        dto.setMaxAdults(room.getMaxAdults());
        dto.setMaxChildren(room.getMaxChildren());
        dto.setQuantity(room.getQuantity());
        dto.setSizeSqm(room.getSizeSqm());
        dto.setFloor(room.getFloor());
        dto.setView(room.getView());
        dto.setPrice(room.getPrice());
        dto.setIsActive(room.getIsActive());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());


        dto.setPhotos(room.getPhotos() == null ? Collections.emptyList() :
                room.getPhotos().stream().map(p -> {
                    RoomResponseDTO.PhotoDTO d = new RoomResponseDTO.PhotoDTO();
                    d.setId(p.getId());
                    d.setUrl(p.getUrl());
                    d.setDisplayOrder(p.getDisplayOrder());
                    d.setCaption(p.getCaption());
                    return d;
                }).collect(Collectors.toList()));


        dto.setAmenities(room.getAmenities() == null ? Collections.emptyList() :
                room.getAmenities().stream().map(a -> {
                    RoomResponseDTO.AmenityDTO d = new RoomResponseDTO.AmenityDTO();
                    d.setId(a.getId());
                    d.setName(a.getName());
                    d.setCategory(a.getCategory().name()); // enum → String ("TECH")
                    d.setIcon(a.getIcon());
                    return d;
                }).collect(Collectors.toList()));


        dto.setAccessibilities(room.getAccessibilities() == null ? Collections.emptyList() :
                room.getAccessibilities().stream().map(a -> {
                    RoomResponseDTO.AccessibilityDTO d = new RoomResponseDTO.AccessibilityDTO();
                    d.setId(a.getId());
                    d.setFeature(a.getFeature());
                    d.setIsAvailable(a.getIsAvailable());
                    return d;
                }).collect(Collectors.toList()));

        return dto;
    }
}