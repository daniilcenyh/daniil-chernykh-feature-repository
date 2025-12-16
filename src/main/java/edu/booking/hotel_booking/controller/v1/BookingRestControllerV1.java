package edu.booking.hotel_booking.controller.v1;

import edu.booking.hotel_booking.dto.request.CreateBookingRequest;
import edu.booking.hotel_booking.dto.request.UpdateBookingRequest;
import edu.booking.hotel_booking.dto.response.BookingResponse;
import edu.booking.hotel_booking.dto.response.RoomResponse;
import edu.booking.hotel_booking.service.RoomService;
import edu.booking.hotel_booking.service.impl.BookingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bookings", description = "API для управления бронированиями")
public class BookingRestControllerV1 {
    private final BookingServiceImpl bookingService;
    private final RoomService roomService;

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Найти доступные номера",
            description = "Возвращает список номеров, доступных для бронирования в указанный период времени"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список доступных номеров успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoomResponse.class, type = "array")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса (даты, вместимость)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка валидации или типа параметра")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<List<RoomResponse>> findAvailableRooms(
            @Parameter(
                    description = "Дата и время начала бронирования (ISO формат: YYYY-MM-DDTHH:MM:SS)",
                    required = true,
                    example = "2024-12-01T14:00:00"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @Parameter(
                    description = "Дата и время окончания бронирования (ISO формат: YYYY-MM-DDTHH:MM:SS)",
                    required = true,
                    example = "2024-12-05T12:00:00"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @Parameter(
                    description = "Минимальная вместимость номера (количество гостей)",
                    required = false,
                    example = "2"
            )
            @RequestParam(required = false, defaultValue = "1")
            @Min(value = 1, message = "Вместимость должна быть не менее 1")
            Integer capacity
    )
    {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.roomService.getAvailableRooms(from, to, capacity));
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать новое бронирование",
            description = "Создает новое бронирование номера для гостя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Бронирование успешно создано",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                Некорректные данные запроса или ошибка бизнес-логики:
                - Некорректный JSON формат
                - Дата бронирования в прошлом
                - Количество гостей превышает вместимость номера
                - Гость не привязан к номеру
                """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка валидации или бизнес-правил")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Гость или номер не найдены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка 'не найден'")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                Конфликт при создании бронирования:
                - Бронирование уже существует
                - Номер недоступен в указанный период
                - Нарушение ограничений базы данных
                """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка конфликта")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<BookingResponse> createNewBooking(
            @Parameter(
                    description = "Данные для создания бронирования",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateBookingRequest.class))
            )
            @RequestBody @Validated CreateBookingRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.bookingService.createBooking(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновить бронирование",
            description = "Обновляет информацию о существующем бронировании"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Бронирование успешно обновлено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                Некорректные данные запроса или ошибка бизнес-логики:
                - Попытка перенести бронирование в прошлое
                - Отмененное бронирование нельзя редактировать
                - Количество гостей превышает вместимость
                """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка валидации или бизнес-правил")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Бронирование с указанным ID не найдено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка 'не найден'")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                Конфликт при обновлении:
                - Новые даты конфликтуют с другими бронированиями
                - Нарушение ограничений базы данных
                """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка конфликта")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<BookingResponse> updateBooking(
            @Parameter(
                    description = "ID бронирования для обновления",
                    required = true,
                    example = "1"
            )
            @PathVariable(name = "id") Long id,

            @Parameter(
                    description = "Новые данные бронирования",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateBookingRequest.class))
            )
            @RequestBody @Validated UpdateBookingRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookingService.updateBooking(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить бронирование",
            description = "Удаляет бронирование по указанному ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Бронирование успешно удалено"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный ID бронирования",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка типа параметра")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Бронирование с указанным ID не найдено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка 'не найден'")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<Void> deleteBooking(
            @Parameter(
                    description = "ID бронирования для удаления",
                    required = true,
                    example = "1"
            )
            @PathVariable(name = "id") Long id
    ) {
        this.bookingService.deleteBooking(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
