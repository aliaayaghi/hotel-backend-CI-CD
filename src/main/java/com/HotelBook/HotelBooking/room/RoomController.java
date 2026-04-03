package com.HotelBook.HotelBooking.room;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;


    @GetMapping
    public ResponseEntity<List<RoomResponseDTO>> getRooms(@PathVariable UUID hotelId) {
        return ResponseEntity.ok(roomService.getRoomsByHotel(hotelId));
    }


    @GetMapping("/all")
    public ResponseEntity<List<RoomResponseDTO>> getAllRooms(@PathVariable UUID hotelId) {
        return ResponseEntity.ok(roomService.getAllRoomsByHotel(hotelId));
    }


    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDTO> getRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.getRoomById(hotelId, roomId));
    }


    @PostMapping
    public ResponseEntity<RoomResponseDTO> createRoom(
            @PathVariable UUID hotelId,
            @Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO created = roomService.createRoom(hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponseDTO> updateRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @Valid @RequestBody RoomRequestDTO request) {
        return ResponseEntity.ok(roomService.updateRoom(hotelId, roomId, request));
    }


    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId) {
        roomService.deleteRoom(hotelId, roomId);
        return ResponseEntity.noContent().build();
    }
}