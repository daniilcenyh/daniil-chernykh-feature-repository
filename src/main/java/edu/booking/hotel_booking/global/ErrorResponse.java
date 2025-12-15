package edu.booking.hotel_booking.global;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String message,
        List<String> details,
        LocalDateTime timestamp,
        String path
) {
    public ErrorResponse(String message) {
        this(message, null, LocalDateTime.now(), null);
    }

    public ErrorResponse(String message, List<String> details) {
        this(message, details, LocalDateTime.now(), null);
    }
}
