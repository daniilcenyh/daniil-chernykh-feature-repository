package edu.booking.hotel_booking.controller.v1;

import edu.booking.hotel_booking.dto.request.CreateGuestRequest;
import edu.booking.hotel_booking.dto.request.UpdateGuestRequest;
import edu.booking.hotel_booking.dto.response.GuestResponse;
import edu.booking.hotel_booking.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guests")
public class GuestRestControllerV1 {
    private final GuestService guestService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GuestResponse> createNewGuest(
            @RequestBody @Validated CreateGuestRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.guestService.createGuest(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GuestResponse> updateGuest(
            @PathVariable(name = "id") Long id,
            @RequestBody @Validated UpdateGuestRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.guestService.updateGuest(id, request));
    }
}
