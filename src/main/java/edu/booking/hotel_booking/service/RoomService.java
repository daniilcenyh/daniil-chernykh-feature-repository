package edu.booking.hotel_booking.service;

import edu.booking.hotel_booking.dto.request.CreateRoomRequest;
import edu.booking.hotel_booking.dto.request.UpdateRoomRequest;
import edu.booking.hotel_booking.dto.response.RoomResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {
    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse updateRoom(Long id, UpdateRoomRequest request);
    void deleteRoom(Long id);
    List<RoomResponse> getAvailableRooms(LocalDateTime from, LocalDateTime to, Integer capacity);
}
