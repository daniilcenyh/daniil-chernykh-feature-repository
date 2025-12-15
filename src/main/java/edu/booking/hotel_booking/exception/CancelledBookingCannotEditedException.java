package edu.booking.hotel_booking.exception;

public class CancelledBookingCannotEditedException extends RuntimeException {
    public CancelledBookingCannotEditedException(String message) {
        super(message);
    }
}
