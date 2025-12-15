package edu.booking.hotel_booking.controller.v1;

import edu.booking.hotel_booking.dto.request.CreateBookingRequest;
import edu.booking.hotel_booking.dto.request.UpdateBookingRequest;
import edu.booking.hotel_booking.dto.response.BookingResponse;
import edu.booking.hotel_booking.dto.response.RoomResponse;
import edu.booking.hotel_booking.service.BookingService;
import edu.booking.hotel_booking.service.RoomService;
import edu.booking.hotel_booking.service.impl.BookingServiceImpl;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingRestControllerV1 {
    private final BookingServiceImpl bookingService;
    private final RoomService roomService;

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<RoomResponse>> findAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @RequestParam(required = false, defaultValue = "1")
            @Min(1) Integer capacity
    )
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.roomService.getAvailableRooms(from, to, capacity));
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookingResponse> createNewBooking(
            @RequestBody @Validated CreateBookingRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.bookingService.createBooking(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable(name = "id") Long id,
            @RequestBody @Validated UpdateBookingRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookingService.updateBooking(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteBooking(@PathVariable(name = "id") Long id) {
        this.bookingService.deleteBooking(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
