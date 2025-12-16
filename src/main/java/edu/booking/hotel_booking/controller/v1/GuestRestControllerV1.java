package edu.booking.hotel_booking.controller.v1;

import edu.booking.hotel_booking.dto.request.CreateGuestRequest;
import edu.booking.hotel_booking.dto.request.UpdateGuestRequest;
import edu.booking.hotel_booking.dto.response.GuestResponse;
import edu.booking.hotel_booking.service.GuestService;
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
@RequestMapping("/api/v1/guests")
public class GuestRestControllerV1 {
    private final GuestService guestService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать нового гостя",
            description = "Регистрирует нового гостя в системе отеля"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Гость успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GuestResponse.class)
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
                    description = "Гость с такими данными уже существует",
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
    public ResponseEntity<GuestResponse> createNewGuest(
            @Parameter(
                    description = "Данные для создания гостя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateGuestRequest.class))
            )
            @RequestBody @Validated CreateGuestRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(this.guestService.createGuest(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновить информацию о госте",
            description = "Обновляет информацию о существующем госте"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о госте успешно обновлена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GuestResponse.class)
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
                    description = "Гость с указанным ID не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(description = "Ошибка 'не найден'")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт при обновлении (например, email уже используется другим гостем)",
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
    public ResponseEntity<GuestResponse> updateGuest(
            @Parameter(
                    description = "ID гостя для обновления",
                    required = true,
                    example = "1"
            )
            @PathVariable(name = "id") Long id,

            @Parameter(
                    description = "Новые данные гостя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateGuestRequest.class))
            )
            @RequestBody @Validated UpdateGuestRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.guestService.updateGuest(id, request));
    }
}
