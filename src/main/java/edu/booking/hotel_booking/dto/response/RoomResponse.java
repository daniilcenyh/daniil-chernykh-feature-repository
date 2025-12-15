package edu.booking.hotel_booking.dto.response;

import edu.booking.hotel_booking.entity.GuestEntity;
import edu.booking.hotel_booking.entity.RoomEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RoomResponse(
        Long id,
        Integer floor,
        String roomNumber,
        Integer capacity,
        LocalDateTime createdAt
) {
    public static RoomResponse fromEntity(RoomEntity room) {
        return RoomResponse.builder()
                .id(room.id())
                .capacity(room.capacity())
                .roomNumber(room.roomNumber())
                .createdAt(room.createdAt())
                .floor(room.floor())
                .build();
    }
}
