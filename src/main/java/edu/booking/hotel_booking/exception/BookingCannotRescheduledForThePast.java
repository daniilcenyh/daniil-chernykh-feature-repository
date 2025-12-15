package edu.booking.hotel_booking.exception;

public class BookingCannotRescheduledForThePast extends RuntimeException {
    public BookingCannotRescheduledForThePast(String message) {
        super(message);
    }
}
