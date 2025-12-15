package edu.booking.hotel_booking.dto.response;

import edu.booking.hotel_booking.dao.BookingRepository;
import edu.booking.hotel_booking.entity.BookingEntity;
import edu.booking.hotel_booking.entity.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BookingResponse(
        Long id,
        Long roomId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BookingStatus status,
        List<Long> guestIds
) {
    public static BookingResponse fromEntity(BookingEntity booking, List<Long> guestIds) {
        return BookingResponse.builder()
                .id(booking.id())
                .roomId(booking.roomId())
                .guestIds(guestIds)
                .startDate(booking.startDate())
                .endDate(booking.endDate())
                .status(booking.status())
                .build();
    }
}
