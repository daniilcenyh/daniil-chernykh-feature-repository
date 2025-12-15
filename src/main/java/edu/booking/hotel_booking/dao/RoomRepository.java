package edu.booking.hotel_booking.dao;

import edu.booking.hotel_booking.dao.mapper.RoomRowMapper;
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
public class RoomRepository extends BaseRepository {

    private static final String TABLE_NAME = "rooms";
    private final RoomRowMapper rowMapper;

    private static final String FIND_BY_ID = """
        SELECT id, floor, room_number, capacity, created_at 
        FROM rooms WHERE id = :id
        """;

    private static final String FIND_ALL = """
        SELECT id, floor, room_number, capacity, created_at 
        FROM rooms ORDER BY floor, room_number
        """;

    private static final String FIND_BY_FLOOR_AND_NUMBER = """
        SELECT id, floor, room_number, capacity, created_at 
        FROM rooms WHERE floor = :floor AND room_number = :roomNumber
        """;

    private static final String UPDATE = """
        UPDATE rooms 
        SET floor = :floor,
            room_number = :roomNumber,
            capacity = :capacity
        WHERE id = :id
        """;

    private static final String DELETE = """
        DELETE FROM rooms WHERE id = :id
        """;

    private static final String EXISTS_BY_FLOOR_AND_NUMBER = """
        SELECT COUNT(*) > 0 FROM rooms 
        WHERE floor = :floor AND room_number = :roomNumber
        """;

    private static final String EXISTS_BY_ID = """
        SELECT COUNT(*) > 0 FROM rooms WHERE id = :id
        """;

    private static final String HAS_ACTIVE_BOOKINGS = """
        SELECT COUNT(*) > 0 FROM bookings b 
        WHERE b.room_id = :roomId AND b.status = 'ACTIVE'
        """;

    public RoomRepository(NamedParameterJdbcTemplate jdbcTemplate,
                          RoomRowMapper rowMapper) {
        super(jdbcTemplate, TABLE_NAME);
        this.rowMapper = rowMapper;
    }

    public RoomEntity save(RoomEntity room) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("floor", room.floor())
                .addValue("roomNumber", room.roomNumber())
                .addValue("capacity", room.capacity());

        if (room.id() == null) {
            // INSERT
            Number key = simpleJdbcInsert.executeAndReturnKey(params);
            return new RoomEntity(
                    key.longValue(),
                    room.floor(),
                    room.roomNumber(),
                    room.capacity(),
                    LocalDateTime.now()
            );
        } else {
            // UPDATE
            params.addValue("id", room.id());
            jdbcTemplate.update(UPDATE, params);

            return findById(room.id())
                    .orElseThrow(() -> new RuntimeException("Room not found after update"));
        }
    }

    public Optional<RoomEntity> findById(Long id) {
        try {
            RoomEntity room = jdbcTemplate.queryForObject(
                    FIND_BY_ID,
                    Map.of("id", id),
                    rowMapper
            );
            return Optional.ofNullable(room);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<RoomEntity> findByFloorAndNumber(Integer floor, String roomNumber) {
        try {
            RoomEntity room = jdbcTemplate.queryForObject(
                    FIND_BY_FLOOR_AND_NUMBER,
                    Map.of("floor", floor, "roomNumber", roomNumber),
                    rowMapper
            );
            return Optional.ofNullable(room);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<RoomEntity> findAll() {
        return jdbcTemplate.query(FIND_ALL, rowMapper);
    }

    public boolean delete(Long id) {
        boolean hasActiveBookings = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                HAS_ACTIVE_BOOKINGS,
                Map.of("roomId", id),
                Boolean.class
        ));

        if (hasActiveBookings) {
            throw new IllegalStateException("Cannot delete room with active bookings");
        }

        int affected = jdbcTemplate.update(
                DELETE,
                Map.of("id", id)
        );
        return affected > 0;
    }

    public boolean existsByFloorAndNumber(Integer floor, String roomNumber) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                EXISTS_BY_FLOOR_AND_NUMBER,
                Map.of("floor", floor, "roomNumber", roomNumber),
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