package edu.booking.hotel_booking.dto.request;

import edu.booking.hotel_booking.entity.enums.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UpdateBookingRequest(
        @Positive(message = "Room ID must be positive")
        Long roomId,

        @FutureOrPresent(message = "Start date must be in the present or future")
        LocalDateTime startDate,

        @Future(message = "End date must be in the future")
        LocalDateTime endDate,

        BookingStatus status,

        @Size(min = 1, message = "At least one guest must be specified")
        List<@NotNull(message = "Guest ID cannot be null")
        @Positive(message = "Guest ID must be positive") Long> guestIds
) {
}
