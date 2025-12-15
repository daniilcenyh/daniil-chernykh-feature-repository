package edu.booking.hotel_booking.exception;

public class GuestIsNotLinkedToTheRoom extends RuntimeException {
    public GuestIsNotLinkedToTheRoom(String message) {
        super(message);
    }
}
