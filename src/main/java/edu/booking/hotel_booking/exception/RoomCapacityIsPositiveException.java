package edu.booking.hotel_booking.exception;

public class RoomCapacityIsPositiveException extends RuntimeException {
    public RoomCapacityIsPositiveException(String message) {
        super(message);
    }
}
