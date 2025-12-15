package edu.booking.hotel_booking.exception;

public class CannotDeleteRoomException extends RuntimeException {
    public CannotDeleteRoomException(String message) {
        super(message);
    }
}
