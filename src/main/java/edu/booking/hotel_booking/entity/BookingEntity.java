package edu.booking.hotel_booking.entity;

import edu.booking.hotel_booking.entity.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookingEntity (
        Long id,
        Long roomId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BookingStatus status,
        LocalDateTime createdAt
) {}
