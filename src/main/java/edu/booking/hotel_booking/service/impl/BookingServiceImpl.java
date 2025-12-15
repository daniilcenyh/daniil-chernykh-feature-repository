package edu.booking.hotel_booking.service.impl;

import edu.booking.hotel_booking.dao.BookingRepository;
import edu.booking.hotel_booking.dao.GuestRepository;
import edu.booking.hotel_booking.dao.RoomRepository;
import edu.booking.hotel_booking.dto.request.CreateBookingRequest;
import edu.booking.hotel_booking.dto.request.UpdateBookingRequest;
import edu.booking.hotel_booking.dto.response.BookingResponse;
import edu.booking.hotel_booking.entity.BookingEntity;
import edu.booking.hotel_booking.entity.enums.BookingStatus;
import edu.booking.hotel_booking.exception.GuestIsNotLinkedToTheRoom;
import edu.booking.hotel_booking.exception.NumberOfGuestExceedsTheCapacity;
import edu.booking.hotel_booking.exception.RoomNotAvailableException;
import edu.booking.hotel_booking.exception.RoomNotFoundException;
import edu.booking.hotel_booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        validateBookingDates(request.startDate(), request.endDate());
        var existedRoom = this.roomRepository.findById(request.roomId())
                .orElseThrow(() -> {
                    return new RoomNotFoundException("");
                });
        if (validateGuestsOfExistence(request.guestIds())) {
            throw new GuestIsNotLinkedToTheRoom("");
        }
        if (existedRoom.capacity() < request.guestIds().size()) {
            throw new NumberOfGuestExceedsTheCapacity("");
        }
        if (this.bookingRepository.isRoomAvailable(existedRoom.id(), request.startDate(), request.endDate())) {
            throw new RoomNotAvailableException("");
        }

        var bookingToSave = BookingEntity.builder()
                .createdAt(LocalDateTime.now())
                .status(BookingStatus.ACTIVE)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .roomId(request.roomId())
                .build();

        BookingEntity saved = this.bookingRepository.save(bookingToSave);
        List<Long> guestIds = new ArrayList<>();
        for (Long id: request.guestIds()) {
            this.bookingRepository.addGuestToBooking(saved.id(), id);
            guestIds.add(id);
        }

        return BookingResponse.fromEntity(saved, guestIds);
    }

    @Override
    public BookingResponse updateBooking(Long id, UpdateBookingRequest request) {
        return null;
    }

    @Override
    public void deleteBooking(Long id) {

    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book in the past");
        }

        if (Duration.between(start, end).toHours() < 1) {
            throw new IllegalArgumentException("Minimum booking duration is 1 hour");
        }
    }

    private boolean validateGuestsOfExistence(List<Long> guestIds) {
        for (Long id: guestIds) {
            if (this.guestRepository.existsById(id)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
