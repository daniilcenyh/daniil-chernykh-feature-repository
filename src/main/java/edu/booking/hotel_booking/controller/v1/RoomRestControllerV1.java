package edu.booking.hotel_booking.controller.v1;

import edu.booking.hotel_booking.dto.request.CreateRoomRequest;
import edu.booking.hotel_booking.dto.request.UpdateRoomRequest;
import edu.booking.hotel_booking.dto.response.RoomResponse;
import edu.booking.hotel_booking.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomRestControllerV1 {
    private final RoomService roomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoomResponse> createNewRoom(
            @RequestBody @Validated CreateRoomRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.roomService.createRoom(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable(name = "id") Long id,
            @RequestBody @Validated UpdateRoomRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteRoom(@PathVariable(name = "id") Long id) {
        this.roomService.deleteRoom(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
