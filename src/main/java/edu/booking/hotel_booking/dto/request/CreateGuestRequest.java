package edu.booking.hotel_booking.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateGuestRequest(
        @NotBlank(message = "First name cannot be blank")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Size(max = 100, message = "Middle name cannot exceed 50 characters")
        String middleName,

        @NotNull(message = "Birth date cannot be null")
        @PastOrPresent(message = "Birth date cannot be in the future")
        LocalDate birthDate,

        @NotBlank(message = "Phone number cannot be blank")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format. Use E.164 format (e.g., +1234567890)")
        String phoneNumber
) {
}
