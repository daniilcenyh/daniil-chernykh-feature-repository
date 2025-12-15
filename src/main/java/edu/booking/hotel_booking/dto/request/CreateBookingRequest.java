package edu.booking.hotel_booking.dto.request;

import edu.booking.hotel_booking.entity.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreateBookingRequest(
        Long roomId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BookingStatus status,
        List<Long> guestIds
) {
}
