package edu.booking.hotel_booking.exception;

public class NumberOfGuestExceedsTheCapacity extends RuntimeException {
    public NumberOfGuestExceedsTheCapacity(String message) {
        super(message);
    }
}
