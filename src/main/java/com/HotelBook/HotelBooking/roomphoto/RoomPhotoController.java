package com.HotelBook.HotelBooking.roomphoto;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/hotels/{hotelId}/rooms/{roomId}/photos")
@RequiredArgsConstructor
public class RoomPhotoController {

    private final RoomPhotoService photoService;


    @GetMapping
    public ResponseEntity<List<RoomPhotoResponseDTO>> getPhotos(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId) {
        return ResponseEntity.ok(photoService.getPhotosByRoom(hotelId, roomId));
    }


    @PostMapping
    public ResponseEntity<RoomPhotoResponseDTO> addPhoto(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @Valid @RequestBody RoomPhotoRequestDTO request) {
        RoomPhotoResponseDTO photo = photoService.addPhoto(hotelId, roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(photo);
    }


    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @PathVariable UUID photoId) {
        photoService.deletePhoto(hotelId, roomId, photoId);
        return ResponseEntity.noContent().build();
    }
}
