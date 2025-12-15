package edu.booking.hotel_booking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record UpdateRoomRequest(
        @Min(value = 1, message = "Floor must be at least 1")
        Integer floor,

        @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Room number must be 1-10 alphanumeric characters")
        String roomNumber,

        @Positive(message = "Capacity must be positive")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity
) {
}
