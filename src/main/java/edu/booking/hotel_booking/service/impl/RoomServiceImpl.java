package edu.booking.hotel_booking.service.impl;

import edu.booking.hotel_booking.dao.BookingRepository;
import edu.booking.hotel_booking.dao.RoomRepository;
import edu.booking.hotel_booking.dto.request.CreateRoomRequest;
import edu.booking.hotel_booking.dto.request.UpdateRoomRequest;
import edu.booking.hotel_booking.dto.response.RoomResponse;
import edu.booking.hotel_booking.entity.RoomEntity;
import edu.booking.hotel_booking.exception.*;
import edu.booking.hotel_booking.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        log.info("Starting creation of new room on floor {}, number {}",
                request.floor(), request.roomNumber());
        log.debug("Room creation request details: capacity={}", request.capacity());
        if (validateRoomByNumberAndFloor(request.floor(), request.roomNumber())) {
            log.warn("Room creation failed - room already exists on floor {}, number {}",
                    request.floor(), request.roomNumber());
            throw new RoomAlreadyExistException(
                    "Room already exists on floor " + request.floor() + " with number " + request.roomNumber()
            );
        }
        if (request.capacity() < 0) {
            log.error("Room creation failed - negative capacity: {}", request.capacity());
            throw new RoomCapacityIsPositiveException(
                    "Room capacity must be positive, got: " + request.capacity()
            );
        }

        var roomToSave = RoomEntity.builder()
                .createdAt(LocalDateTime.now())
                .capacity(request.capacity())
                .floor(request.floor())
                .roomNumber(request.roomNumber())
                .build();
        log.debug("Attempting to save room entity to database");
        var savedRoom = this.roomRepository.save(roomToSave);
        log.info("Room created successfully with ID: {}", savedRoom.id());
        log.debug("Room created with details: ID={}, floor={}, number={}, capacity={}",
                savedRoom.id(), savedRoom.floor(), savedRoom.roomNumber(), savedRoom.capacity());

        return RoomResponse.fromEntity(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        log.info("Starting update for room with ID: {}", id);
        log.debug("Update request details for room ID {}: floor={}, number={}, capacity={}",
                id, request.floor(), request.roomNumber(), request.capacity());

        var existedRoom = this.roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Room not found for update - ID: {}", id);
                    return new RoomNotFoundException("Room with id: " + id + " not found.");
                });
        log.debug("Found existing room: ID={}, currentFloor={}, currentNumber={}, currentCapacity={}",
                id, existedRoom.floor(), existedRoom.roomNumber(), existedRoom.capacity());

        if ((request.roomNumber() != null && request.floor() != null) && (!request.floor().equals(existedRoom.floor()) || !request.roomNumber().equals(existedRoom.roomNumber()))) {
            log.debug("Checking uniqueness for new floor/number combination: floor={}, number={}",
                    request.floor(), request.roomNumber());
            if (validateRoomByNumberAndFloor(request.floor(), request.roomNumber())) {
                log.warn("Room update failed - room already exists on floor {}, number {}",
                        request.floor(), request.roomNumber());
                throw new RoomAlreadyExistException(
                        "Room already exists on floor " + request.floor() + " with number " + request.roomNumber()
                );
            }
        }

        var roomToUpdate = RoomEntity.builder()
                .id(id)
                .roomNumber(request.roomNumber() != null ? request.roomNumber() : existedRoom.roomNumber())
                .floor(request.floor() != null ? request.floor() : existedRoom.floor())
                .capacity((request.capacity() != null) && (request.capacity() >= existedRoom.capacity()) ? request.capacity() : existedRoom.capacity())
                .build();
        log.debug("Attempting to update room entity in database");
        var updatedRoom = this.roomRepository.save(roomToUpdate);
        log.info("Room updated successfully - ID: {}", id);
        log.debug("Room updated with new details: floor={}, number={}, capacity={}",
                updatedRoom.floor(), updatedRoom.roomNumber(), updatedRoom.capacity());

        return RoomResponse.fromEntity(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        log.info("Attempting to delete room with ID: {}", id);
        var existedRoom = this.roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Room not found for deletion - ID: {}", id);
                    return new RoomNotFoundException("Room with id: " + id + " not found.");
                });
        log.debug("Found room to delete: ID={}, floor={}, number={}",
                id, existedRoom.floor(), existedRoom.roomNumber());

        var existedBooking = this.bookingRepository.findByRoomId(id)
                .orElseThrow(() -> {
                    log.debug("No bookings found for room ID: {}", id);
                    return new BookingNotFoundException("");
                });

        if (this.bookingRepository.isRoomAvailable(existedRoom.id(), existedBooking.startDate(), existedBooking.endDate())) {
            log.warn("Cannot delete room ID {} - has active bookings", id);
            throw new CannotDeleteRoomException(
                    "Cannot delete room with active bookings. Room ID: " + id
            );
        }

        log.debug("Attempting to delete room from database");
        this.roomRepository.delete(id);
        log.info("Room deleted successfully - ID: {}", id);

    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms(LocalDateTime from, LocalDateTime to, Integer capacity) {
        log.info("Searching for available rooms from {} to {} with capacity {}", from, to, capacity);
        validateBookingDates(from, to, capacity);

        log.debug("Querying database for available rooms");
        List<RoomEntity> rooms = this.bookingRepository.findAvailableRooms(from, to, capacity);

        log.info("Found {} available rooms for the specified criteria", rooms.size());
        log.debug("Available room IDs: {}", rooms.stream().map(RoomEntity::id).toList());
        return rooms.stream()
                .map(RoomResponse::fromEntity)
                .toList();
    }

    private boolean validateRoomByNumberAndFloor(Integer floor, String roomNumber) {
        log.trace("Validating if room exists on floor {}, number {}", floor, roomNumber);
        return this.roomRepository.existsByFloorAndNumber(floor, roomNumber);
    }

    private void validateBookingDates(LocalDateTime from, LocalDateTime to, Integer capacity) {
        if (from == null || to == null) {
            log.error("Invalid date range provided: from={}, to={}", from, to);
            throw new IllegalArgumentException("Date range cannot be null");
        }

        if (from.isAfter(to)) {
            log.error("Invalid date range: start date {} is after end date {}", from, to);
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (capacity != null && capacity < 0) {
            log.error("Invalid capacity requested: {}", capacity);
            throw new IllegalArgumentException("Capacity must be non-negative");
        }
    }
}
