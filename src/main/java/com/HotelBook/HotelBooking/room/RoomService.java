package com.HotelBook.HotelBooking.room;




import java.util.List;
import java.util.UUID;


public interface RoomService {

    RoomResponseDTO createRoom(UUID hotelId, RoomRequestDTO request);
    RoomResponseDTO getRoomById(UUID hotelId, UUID roomId);
    List<RoomResponseDTO> getRoomsByHotel(UUID hotelId);
    List<RoomResponseDTO> getAllRoomsByHotel(UUID hotelId);
    RoomResponseDTO updateRoom(UUID hotelId, UUID roomId, RoomRequestDTO request);
    void deleteRoom(UUID hotelId, UUID roomId);
}
