package edu.booking.hotel_booking.dto.request;

import edu.booking.hotel_booking.entity.enums.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreateBookingRequest(
        @NotNull(message = "Room ID cannot be null")
        @Positive(message = "Room ID must be positive")
        Long roomId,

        @NotNull(message = "Start date cannot be null")
        @FutureOrPresent(message = "Start date must be in the present or future")
        LocalDateTime startDate,

        @NotNull(message = "End date cannot be null")
        @Future(message = "End date must be in the future")
        LocalDateTime endDate,

        BookingStatus status,

        @NotNull(message = "Guest list cannot be null")
        @Size(min = 1, message = "At least one guest must be specified")
        List<@NotNull(message = "Guest ID cannot be null")
        @Positive(message = "Guest ID must be positive") Long> guestIds
) {
}
