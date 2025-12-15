package edu.booking.hotel_booking.global;

import edu.booking.hotel_booking.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * <strong>Ошибки валидации</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        String message = "Validation failed. Please check your input.";

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                errors,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * <strong>Ошибки 404 кода</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler({
            GuestNotFoundException.class,
            RoomNotFoundException.class,
            BookingNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            RuntimeException ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                null,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * <strong>Ошибки бизнес логики</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler({
            RoomNotAvailableException.class,
            BookingAlreadyExistException.class,
            RoomAlreadyExistException.class,
            GuestAlreadyExistException.class,
            CannotDeleteRoomException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictException(
            RuntimeException ex,
            WebRequest request) {

        String message = ex.getMessage();

        if (ex instanceof DataIntegrityViolationException) {
            if (ex.getMessage().contains("unique constraint")) {
                message = "Duplicate entry. The resource already exists.";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Cannot delete or update. Related records exist.";
            } else {
                message = "Database constraint violation.";
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                null,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.warn("Conflict: {}", message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * <strong>Превышение вместимости</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler({
            NumberOfGuestExceedsTheCapacity.class,
            RoomCapacityIsPositiveException.class
    })
    public ResponseEntity<ErrorResponse> handleCapacityExceptions(
            RuntimeException ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                null,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.warn("Capacity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * <strong>Ошибки бронирования</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler({
            BookingCannotRescheduledForThePast.class,
            GuestIsNotLinkedToTheRoom.class,
            CancelledBookingCannotEditedException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBookingExceptions(
            RuntimeException ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                null,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.warn("Booking error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * <strong>Ошибка при пустом результате из БД</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleEmptyResult(
            EmptyResultDataAccessException ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "Requested resource was not found",
                null,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.warn("Empty result from database: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * <strong>Некорректный JSON</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseException(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        String message = "Invalid JSON format in request body";
        if (ex.getMessage() != null && ex.getMessage().contains("LocalDateTime")) {
            message = "Invalid date format. Please use ISO format: yyyy-MM-dd'T'HH:mm:ss";
        }

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                null,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.warn("JSON parsing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * <strong>Некорректный тип параметра</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {

        String message = String.format(
                "Parameter '%s' has invalid value '%s'. Expected type: %s",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        ErrorResponse errorResponse = new ErrorResponse(
                message,
                null,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.warn("Type mismatch: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * <strong>Остальные ошибки</strong>
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex,
            WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "Internal server error. Please contact support.",
                null,
                LocalDateTime.now(),
                getRequestPath(request)
        );

        log.error("Unexpected error occurred at {}: {}",
                getRequestPath(request), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
            return servletRequest.getRequestURI();
        }
        return "unknown";
    }
}