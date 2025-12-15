package edu.booking.hotel_booking.exception;

public class RoomAlreadyExistException extends RuntimeException {
    public RoomAlreadyExistException(String message) {
        super(message);
    }
}
