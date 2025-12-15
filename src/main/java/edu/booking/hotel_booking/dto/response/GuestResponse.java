package edu.booking.hotel_booking.dto.response;

import edu.booking.hotel_booking.entity.GuestEntity;

import java.time.LocalDate;

public record GuestResponse (
        Long id,
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        String phoneNumber
) {
    public static GuestResponse fromEntity(GuestEntity guest) {
        return new GuestResponse(
                guest.id(),
                guest.firstName(),
                guest.lastName(),
                guest.middleName(),
                guest.birthDate(),
                guest.phoneNumber()
        );
    }
}
