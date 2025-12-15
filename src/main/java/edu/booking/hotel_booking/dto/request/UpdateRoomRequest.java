package edu.booking.hotel_booking.dto.request;

public record UpdateRoomRequest(
        Integer floor,
        String roomNumber,
        Integer capacity
) {
}
