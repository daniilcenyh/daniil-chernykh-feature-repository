package edu.booking.hotel_booking.entity;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RoomEntity (
        Long id,
        Integer floor,
        String roomNumber,
        Integer capacity,
        LocalDateTime createdAt
) {}
