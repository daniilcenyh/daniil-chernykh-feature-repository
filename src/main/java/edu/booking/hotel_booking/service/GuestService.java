package edu.booking.hotel_booking.service;

import edu.booking.hotel_booking.dto.request.CreateGuestRequest;
import edu.booking.hotel_booking.dto.request.UpdateGuestRequest;
import edu.booking.hotel_booking.dto.response.GuestResponse;

import java.util.List;

public interface GuestService {
    GuestResponse createGuest(CreateGuestRequest request);
    GuestResponse updateGuest(Long id, UpdateGuestRequest request);
    GuestResponse getGuest(Long id);
    List<GuestResponse> getAllGuests();
}
