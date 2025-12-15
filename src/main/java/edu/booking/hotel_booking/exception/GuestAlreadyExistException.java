package edu.booking.hotel_booking.exception;

public class GuestAlreadyExistException extends RuntimeException {
    public GuestAlreadyExistException(String message) {
        super(message);
    }
}
