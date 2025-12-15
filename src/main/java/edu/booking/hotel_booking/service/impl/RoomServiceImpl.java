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
        if (validateRoomByNumberAndFloor(request.floor(), request.roomNumber())) {
            throw new RoomAlreadyExistException("");
        }
        if (request.capacity() < 0) {
            throw new RoomCapacityIsPositiveException("");
        }

        var roomToSave = RoomEntity.builder()
                .createdAt(LocalDateTime.now())
                .capacity(request.capacity())
                .floor(request.floor())
                .roomNumber(request.roomNumber())
                .build();
        var savedRoom = this.roomRepository.save(roomToSave);
        return RoomResponse.fromEntity(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        var existedRoom = this.roomRepository.findById(id)
                .orElseThrow(() -> {
                    return new RoomNotFoundException("");
                });

        if ((request.roomNumber() != null && request.floor() != null) && (!request.floor().equals(existedRoom.floor()) || !request.roomNumber().equals(existedRoom.roomNumber()))) {
            if (validateRoomByNumberAndFloor(request.floor(), request.roomNumber())) {
                throw new RoomAlreadyExistException("");
            }
        }

        var roomToUpdate = RoomEntity.builder()
                .id(id)
                .roomNumber(request.roomNumber() != null ? request.roomNumber() : existedRoom.roomNumber())
                .floor(request.floor() != null ? request.floor() : existedRoom.floor())
                .capacity((request.capacity() != null) && (request.capacity() >= existedRoom.capacity()) ? request.capacity() : existedRoom.capacity())
                .build();
        var updatedRoom = this.roomRepository.save(roomToUpdate);
        return RoomResponse.fromEntity(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        var existedRoom = this.roomRepository.findById(id)
                .orElseThrow(() -> {
                    return new RoomNotFoundException("");
                });
        var existedBooking = this.bookingRepository.findByRoomId(id)
                .orElseThrow(() -> {
                    return new BookingNotFoundException("");
                });

        if (this.bookingRepository.isRoomAvailable(existedRoom.id(), existedBooking.startDate(), existedBooking.endDate())) {
            throw new CannotDeleteRoomException("");
        }

        this.bookingRepository.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms(LocalDateTime from, LocalDateTime to, Integer capacity) {
        List<RoomEntity> rooms = this.bookingRepository.findAvailableRooms(from, to, capacity);
        return rooms.stream()
                .map(RoomResponse::fromEntity)
                .toList();
    }

    private boolean validateRoomByNumberAndFloor(Integer floor, String roomNumber) {
        return this.roomRepository.existsByFloorAndNumber(floor, roomNumber);
    }
}
