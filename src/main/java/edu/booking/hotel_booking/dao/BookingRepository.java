package edu.booking.hotel_booking.dao;

import edu.booking.hotel_booking.dao.mapper.BookingRowMapper;
import edu.booking.hotel_booking.dao.mapper.RoomRowMapper;
import edu.booking.hotel_booking.entity.BookingEntity;
import edu.booking.hotel_booking.entity.RoomEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BookingRepository extends BaseRepository {

    private static final String TABLE_NAME = "bookings";
    private final BookingRowMapper bookingRowMapper;
    private final RoomRowMapper roomRowMapper;

    // Основные SQL запросы
    private static final String FIND_BY_ID = """
        SELECT id, room_id, start_date, end_date, status, created_at 
        FROM bookings WHERE id = :id
        """;

    private static final String FIND_BY_ROOM_ID = """
            SELECT id, room_id, start_date, end_date, status, created_at 
            FROM bookings WHERE room_id = :room_id
            """;

    private static final String FIND_ALL = """
        SELECT id, room_id, start_date, end_date, status, created_at 
        FROM bookings ORDER BY start_date DESC
        """;

    private static final String UPDATE = """
        UPDATE bookings 
        SET room_id = :roomId,
            start_date = :startDate,
            end_date = :endDate,
            status = :status
        WHERE id = :id
        """;

    private static final String DELETE = """
        DELETE FROM bookings WHERE id = :id
        """;

    private static final String SOFT_DELETE = """
        UPDATE bookings SET status = 'CANCELLED' WHERE id = :id
        """;

    private static final String EXISTS_BY_ID = """
        SELECT COUNT(*) > 0 FROM bookings WHERE id = :id
        """;

    private static final String FIND_AVAILABLE_ROOMS = """
        SELECT r.id, r.floor, r.room_number, r.capacity, r.created_at 
        FROM rooms r
        WHERE r.capacity >= :capacity
        AND NOT EXISTS (
            SELECT 1 FROM bookings b
            WHERE b.room_id = r.id
            AND b.status = 'ACTIVE'
            AND b.start_date < :endDate
            AND b.end_date > :startDate
        )
        ORDER BY r.floor, r.room_number
        """;

    // Проверка доступности конкретного номера
    private static final String IS_ROOM_AVAILABLE = """
        SELECT COUNT(*) = 0 FROM bookings b
        WHERE b.room_id = :roomId
        AND b.status = 'ACTIVE'
        AND b.start_date < :endDate
        AND b.end_date > :startDate
        """;

    // Проверка доступности номера при обновлении (исключая текущую бронь)
    private static final String IS_ROOM_AVAILABLE_EXCLUDING = """
        SELECT COUNT(*) = 0 FROM bookings b
        WHERE b.room_id = :roomId
        AND b.status = 'ACTIVE'
        AND b.id != :excludeBookingId
        AND b.start_date < :endDate
        AND b.end_date > :startDate
        """;

    private static final String ADD_GUEST_TO_BOOKING = """
        INSERT INTO booking_guests (booking_id, guest_id) 
        VALUES (:bookingId, :guestId)
        """;

    private static final String REMOVE_GUEST_FROM_BOOKING = """
        DELETE FROM booking_guests 
        WHERE booking_id = :bookingId AND guest_id = :guestId
        """;

    private static final String GET_GUEST_IDS_BY_BOOKING = """
        SELECT guest_id FROM booking_guests WHERE booking_id = :bookingId
        """;

    private static final String REMOVE_ALL_GUESTS_FROM_BOOKING = """
        DELETE FROM booking_guests WHERE booking_id = :bookingId
        """;

    private static final String HAS_GUEST_IN_BOOKING = """
        SELECT COUNT(*) > 0 FROM booking_guests 
        WHERE booking_id = :bookingId AND guest_id = :guestId
        """;

    public BookingRepository(NamedParameterJdbcTemplate jdbcTemplate,
                             BookingRowMapper bookingRowMapper,
                             RoomRowMapper roomRowMapper) {
        super(jdbcTemplate, TABLE_NAME);
        this.bookingRowMapper = bookingRowMapper;
        this.roomRowMapper = roomRowMapper;
    }

    public BookingEntity save(BookingEntity booking) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("roomId", booking.roomId())
                .addValue("startDate", booking.startDate())
                .addValue("endDate", booking.endDate())
                .addValue("status", booking.status());

        if (booking.id() == null) {
            // INSERT
            Number key = simpleJdbcInsert.executeAndReturnKey(params);
            return new BookingEntity(
                    key.longValue(),
                    booking.roomId(),
                    booking.startDate(),
                    booking.endDate(),
                    booking.status(),
                    LocalDateTime.now()
            );
        } else {
            // UPDATE
            params.addValue("id", booking.id());
            jdbcTemplate.update(UPDATE, params);

            return findById(booking.id())
                    .orElseThrow(() -> new RuntimeException("Booking not found after update"));
        }
    }

    public Optional<BookingEntity> findById(Long id) {
        try {
            BookingEntity booking = jdbcTemplate.queryForObject(
                    FIND_BY_ID,
                    Map.of("id", id),
                    bookingRowMapper
            );
            return Optional.ofNullable(booking);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<BookingEntity> findByRoomId(Long roomId) {
        try {
            BookingEntity booking = jdbcTemplate.queryForObject(
                    FIND_BY_ROOM_ID,
                    Map.of("room_id", roomId),
                    bookingRowMapper
            );
            return Optional.ofNullable(booking);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<BookingEntity> findAll() {
        return jdbcTemplate.query(FIND_ALL, bookingRowMapper);
    }

    public boolean cancel(Long id) {
        int affected = jdbcTemplate.update(
                SOFT_DELETE,
                Map.of("id", id)
        );
        return affected > 0;
    }

    public boolean delete(Long id) {
        // Сначала удаляем связи с гостями
        jdbcTemplate.update(
                REMOVE_ALL_GUESTS_FROM_BOOKING,
                Map.of("bookingId", id)
        );

        // Затем удаляем саму бронь
        int affected = jdbcTemplate.update(
                DELETE,
                Map.of("id", id)
        );
        return affected > 0;
    }

    // Основные бизнес-методы

    public List<RoomEntity> findAvailableRooms(LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               Integer capacity) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("capacity", capacity != null ? capacity : 1);

        return jdbcTemplate.query(FIND_AVAILABLE_ROOMS, params, roomRowMapper);
    }

    public boolean isRoomAvailable(Long roomId,
                                   LocalDateTime startDate,
                                   LocalDateTime endDate) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("roomId", roomId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                IS_ROOM_AVAILABLE, params, Boolean.class
        ));
    }

    public boolean isRoomAvailableExcluding(Long roomId,
                                            LocalDateTime startDate,
                                            LocalDateTime endDate,
                                            Long excludeBookingId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("roomId", roomId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("excludeBookingId", excludeBookingId);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                IS_ROOM_AVAILABLE_EXCLUDING, params, Boolean.class
        ));
    }

    public void addGuestToBooking(Long bookingId, Long guestId) {
        jdbcTemplate.update(
                ADD_GUEST_TO_BOOKING,
                Map.of("bookingId", bookingId, "guestId", guestId)
        );
    }

    public void removeGuestFromBooking(Long bookingId, Long guestId) {
        jdbcTemplate.update(
                REMOVE_GUEST_FROM_BOOKING,
                Map.of("bookingId", bookingId, "guestId", guestId)
        );
    }

    public List<Long> getGuestIdsByBooking(Long bookingId) {
        return jdbcTemplate.queryForList(
                GET_GUEST_IDS_BY_BOOKING,
                Map.of("bookingId", bookingId),
                Long.class
        );
    }

    public void removeAllGuestsFromBooking(Long bookingId) {
        jdbcTemplate.update(
                REMOVE_ALL_GUESTS_FROM_BOOKING,
                Map.of("bookingId", bookingId)
        );
    }

    public boolean hasGuestInBooking(Long bookingId, Long guestId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                HAS_GUEST_IN_BOOKING,
                Map.of("bookingId", bookingId, "guestId", guestId),
                Boolean.class
        ));
    }

    public boolean existsById(Long id) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                EXISTS_BY_ID,
                Map.of("id", id),
                Boolean.class
        ));
    }
}