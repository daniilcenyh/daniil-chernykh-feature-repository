package edu.booking.hotel_booking.controller.v1;

import edu.booking.hotel_booking.dto.request.CreateRoomRequest;
import edu.booking.hotel_booking.dto.request.UpdateRoomRequest;
import edu.booking.hotel_booking.dto.response.RoomResponse;
import edu.booking.hotel_booking.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Создать новый номер",
            description = "Создает новый номер отеля с указанными параметрами"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Номер успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка валидации")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт: номер с такими параметрами уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка бизнес-логики")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Общая ошибка сервера")
                    )
            )
    })
    public ResponseEntity<RoomResponse> createNewRoom(
            @Parameter(
                    description = "Данные для создания номера",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateRoomRequest.class))
            )
            @RequestBody @Validated CreateRoomRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.roomService.createRoom(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновить информацию о номере",
            description = "Обновляет информацию о существующем номере отеля"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о номере успешно обновлена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoomResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка валидации")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Номер с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка 'не найден'")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт при обновлении номера",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка бизнес-логики")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<RoomResponse> updateRoom(
            @Parameter(
                    description = "ID номера для обновления",
                    required = true,
                    example = "1"
            )
            @PathVariable(name = "id") Long id,

            @Parameter(
                    description = "Новые данные номера",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateRoomRequest.class))
            )
            @RequestBody @Validated UpdateRoomRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить номер",
            description = "Удаляет номер отеля по указанному ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Номер успешно удален"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный ID номера",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка типа параметра")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Номер с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка 'не найден'")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Невозможно удалить номер (есть активные бронирования)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка бизнес-логики")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    public ResponseEntity<Void> deleteRoom(
            @Parameter(
                    description = "ID номера для удаления",
                    required = true,
                    example = "1"
            )
            @PathVariable(name = "id") Long id
    ) {
        this.roomService.deleteRoom(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
