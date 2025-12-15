package edu.booking.hotel_booking.service;

import edu.booking.hotel_booking.dto.request.CreateBookingRequest;
import edu.booking.hotel_booking.dto.request.UpdateBookingRequest;
import edu.booking.hotel_booking.dto.response.BookingResponse;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
    BookingResponse updateBooking(Long id, UpdateBookingRequest request);
    void deleteBooking(Long id);
}
