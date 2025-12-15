package edu.booking.hotel_booking.dto.request;

import java.time.LocalDateTime;

public record CreateRoomRequest(
        Integer floor,
        String roomNumber,
        Integer capacity
) {
}
