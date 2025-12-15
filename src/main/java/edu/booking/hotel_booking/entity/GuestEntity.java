package edu.booking.hotel_booking.entity;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record GuestEntity (
        Long id,
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        String phoneNumber,
        LocalDateTime createdAt
) {}
