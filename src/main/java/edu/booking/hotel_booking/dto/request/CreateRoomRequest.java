package edu.booking.hotel_booking.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateRoomRequest(
        @NotNull(message = "Floor cannot be null")
        @Min(value = 1, message = "Floor must be at least 1")
        Integer floor,

        @NotBlank(message = "Room number cannot be blank")
        @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Room number must be 1-10 alphanumeric characters")
        String roomNumber,

        @NotNull(message = "Capacity cannot be null")
        @Positive(message = "Capacity must be positive")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity
) {
}
