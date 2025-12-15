package edu.booking.hotel_booking.service.impl;

import edu.booking.hotel_booking.dao.BookingRepository;
import edu.booking.hotel_booking.dao.GuestRepository;
import edu.booking.hotel_booking.dao.RoomRepository;
import edu.booking.hotel_booking.dto.request.CreateBookingRequest;
import edu.booking.hotel_booking.dto.request.UpdateBookingRequest;
import edu.booking.hotel_booking.dto.response.BookingResponse;
import edu.booking.hotel_booking.entity.BookingEntity;
import edu.booking.hotel_booking.entity.enums.BookingStatus;
import edu.booking.hotel_booking.exception.*;
import edu.booking.hotel_booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        log.info("Starting creation of new booking for room ID: {} from {} to {}",
                request.roomId(), request.startDate(), request.endDate());
        log.debug("Booking creation details: guestIds={}", request.guestIds());

        validateBookingDates(request.startDate(), request.endDate());
        var existedRoom = this.roomRepository.findById(request.roomId())
                .orElseThrow(() -> {
                    log.error("Room not found for booking creation - ID: {}", request.roomId());
                    return new RoomNotFoundException("Room with ID: " + request.roomId() + " not found.");
                });
        log.debug("Found room: ID={}, floor={}, number={}, capacity={}",
                existedRoom.id(), existedRoom.floor(),
                existedRoom.roomNumber(), existedRoom.capacity());

        if (validateGuestsOfExistence(request.guestIds())) {
            List<Long> invalidGuestIds = request.guestIds().stream()
                    .filter(guestId -> !guestRepository.existsById(guestId))
                    .toList();
            log.error("Booking creation failed - invalid guest IDs: {}", invalidGuestIds);
            throw new GuestIsNotLinkedToTheRoom(
                    "One or more guests do not exist. Invalid guest IDs: " + invalidGuestIds
            );
        }

        if (existedRoom.capacity() < request.guestIds().size()) {
            log.error("Booking creation failed - {} guests exceed room capacity {}",
                    request.guestIds().size(), existedRoom.capacity());
            throw new NumberOfGuestExceedsTheCapacity(
                    "Number of guests (" + request.guestIds().size() +
                            ") exceeds room capacity (" + existedRoom.capacity() + ")"
            );
        }

        if (this.bookingRepository.isRoomAvailable(existedRoom.id(), request.startDate(), request.endDate())) {
            log.warn("Room {} not available for booking from {} to {}",
                    request.roomId(), request.startDate(), request.endDate());

            var alternativeRooms = this.bookingRepository.findAvailableRooms(
                    request.startDate(),
                    request.endDate(),
                    existedRoom.capacity()
            );

            String suggestion = alternativeRooms.isEmpty()
                    ? "No alternative rooms available for the selected dates."
                    : String.format("Consider these available rooms: %s",
                    alternativeRooms.stream()
                            .map(r -> String.format("#%d (floor %d, capacity %d)",
                                    r.id(), r.floor(), r.capacity()))
                            .collect(Collectors.joining(", ")));

            throw new RoomNotAvailableException(
                    String.format("Room %d is not available for the selected dates. %s",
                            request.roomId(), suggestion)
            );
        }

        var bookingToSave = BookingEntity.builder()
                .createdAt(LocalDateTime.now())
                .status(BookingStatus.ACTIVE)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .roomId(request.roomId())
                .build();

        log.debug("Attempting to save booking entity to database");
        var saved = this.bookingRepository.save(bookingToSave);
        log.info("Booking created successfully with ID: {}", saved.id());

        List<Long> guestIds = new ArrayList<>();
        log.debug("Linking {} guests to booking ID: {}", request.guestIds().size(), saved.id());
        for (Long guestId : request.guestIds()) {
            this.bookingRepository.addGuestToBooking(saved.id(), guestId);
            guestIds.add(guestId);
            log.trace("Guest ID {} linked to booking ID {}", guestId, saved.id());
        }

        log.info("Booking {} completed: room ID {}, {} guests, duration {} hours",
                saved.id(), saved.roomId(), guestIds.size(),
                Duration.between(saved.startDate(), saved.endDate()).toHours());

        return BookingResponse.fromEntity(saved, guestIds);
    }

    @Override
    public BookingResponse updateBooking(Long id, UpdateBookingRequest request) {
        log.info("Starting update for booking ID: {}", id);
        log.debug("Update request details: roomId={}, startDate={}, endDate={}, status={}, guestIds={}",
                request.roomId(), request.startDate(), request.endDate(),
                request.status(), request.guestIds());

        var existedBooking = this.bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found for update - ID: {}", id);
                    return new BookingNotFoundException("Booking with ID: " + id + " not found.");
                });

        log.debug("Found existing booking: roomId={}, status={}, dates={} to {}",
                existedBooking.roomId(), existedBooking.status(),
                existedBooking.startDate(), existedBooking.endDate());

        if (BookingStatus.CANCELLED.equals(existedBooking.status())) {
            log.error("Attempt to update cancelled booking {}", id);
            throw new CancelledBookingCannotEditedException(
                    "Cannot update cancelled booking. Please create a new one."
            );
        }

        validateBookingDates(request.startDate(), request.endDate());

        if (request.startDate().isAfter(LocalDateTime.now())) {
            log.error("Attempt to move booking {} to past: {}", id, request.startDate());
            throw new BookingCannotRescheduledForThePast(
                    "Booking start date cannot be in the past. Current time is " + LocalDateTime.now()
            );
        }

        var existedRoom = this.roomRepository.findById(request.roomId())
                .orElseThrow(() -> {
                    log.error("Room not found for booking update - ID: {}", request.roomId());
                    return new RoomNotFoundException("Room with ID: " + request.roomId() + " not found.");
                });

        log.debug("Using room: ID={}, capacity={}", existedRoom.id(), existedRoom.capacity());

        if (!request.roomId().equals(existedBooking.roomId()) ||
                ((!request.startDate().equals(existedBooking.startDate())) || (!request.endDate().equals(existedBooking.endDate())))
        ) {
            if (this.bookingRepository.isRoomAvailableExcluding(request.roomId(),
                    request.startDate(),
                    request.endDate(),
                    id
            )) {
                log.warn("Room {} not available for booking update {} from {} to {}",
                        request.roomId(), id, request.startDate(), request.endDate());
                var alternativeRooms = this.bookingRepository.findAvailableRooms(
                        request.startDate(),
                        request.endDate(),
                        existedRoom.capacity()
                );
                String suggestion = alternativeRooms.isEmpty()
                        ? "No alternative rooms available."
                        : String.format("Consider rooms: %s",
                        alternativeRooms.stream()
                                .map(r -> String.format("#%d (floor %d)", r.id(), r.floor()))
                                .collect(Collectors.joining(", ")));

                throw new RoomNotAvailableException(
                        String.format("Room %d is already booked for selected dates. %s",
                                request.roomId(), suggestion)
                );
            }
        }

        if (validateGuestsOfExistence(request.guestIds())) {
            List<Long> invalidGuestIds = request.guestIds().stream()
                    .filter(guestId -> !guestRepository.existsById(guestId))
                    .toList();
            log.error("Booking update failed - invalid guest IDs: {}", invalidGuestIds);
            throw new GuestIsNotLinkedToTheRoom(
                    "One or more guests do not exist. Invalid guest IDs: " + invalidGuestIds
            );
        }
        if (existedRoom.capacity() < request.guestIds().size()) {
            log.error("Booking {} update failed: {} guests exceed room capacity {}",
                    id, request.guestIds().size(), existedRoom.capacity());
            throw new NumberOfGuestExceedsTheCapacity(
                    "Number of guests (" + request.guestIds().size() +
                            ") exceeds room capacity (" + existedRoom.capacity() + ")"
            );
        }

        log.debug("Updating guest list for booking ID: {}", id);
        this.bookingRepository.removeAllGuestsFromBooking(id);
        for (Long guestId : request.guestIds()) {
            this.bookingRepository.addGuestToBooking(id, guestId);
            log.trace("Guest ID {} added to booking ID {}", guestId, id);
        }

        var bookingToUpdate = BookingEntity.builder()
                .id(id)
                .roomId(request.roomId() != null ? request.roomId() : existedBooking.roomId())
                .status(request.status() != null ? request.status() : existedBooking.status())
                .startDate(request.startDate() != null ? request.startDate() : existedBooking.startDate())
                .endDate(request.endDate() != null ? request.endDate() : existedBooking.endDate())
                .createdAt(existedBooking.createdAt())
                .build();

        log.debug("Attempting to update booking entity in database");
        var updated = this.bookingRepository.save(bookingToUpdate);
        log.info("Booking updated successfully - ID: {}", id);

        var currentGuestIds = request.guestIds() != null && !request.guestIds().isEmpty()
                ? request.guestIds()
                : this.bookingRepository.getGuestIdsByBooking(id);

        log.debug("Booking {} updated: roomId={}, status={}, {} guests",
                id, updated.roomId(), updated.status(), currentGuestIds.size());

        return BookingResponse.fromEntity(updated, currentGuestIds);
    }

    @Override
    public void deleteBooking(Long id) {
        log.info("Attempting to delete/cancel booking ID: {}", id);
        if (!this.bookingRepository.existsById(id)) {
            log.error("Booking not found for deletion - ID: {}", id);
            throw new BookingNotFoundException("Booking with ID: " + id + " not found.");
        }

        log.debug("Cancelling booking ID: {}", id);
        this.bookingRepository.cancel(id);
        log.info("Booking cancelled successfully - ID: {}", id);
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
