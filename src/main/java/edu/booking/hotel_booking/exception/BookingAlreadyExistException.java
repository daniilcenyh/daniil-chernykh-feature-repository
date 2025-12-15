package edu.booking.hotel_booking.exception;

public class BookingAlreadyExistException extends RuntimeException {
    public BookingAlreadyExistException(String message) {
        super(message);
    }
}
