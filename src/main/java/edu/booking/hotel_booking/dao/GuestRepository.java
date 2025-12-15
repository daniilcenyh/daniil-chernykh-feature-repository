package edu.booking.hotel_booking.dao;

import edu.booking.hotel_booking.dao.mapper.GuestRowMapper;
import edu.booking.hotel_booking.entity.GuestEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class GuestRepository extends BaseRepository {

    private static final String TABLE_NAME = "guest";
    private final GuestRowMapper rowMapper;

    // SQL queries
    private static final String FIND_BY_ID = """
        SELECT id, first_name, last_name, middle_name, birth_date, phone_number, created_at 
        FROM guest WHERE id = :id
        """;

    private static final String FIND_BY_PHONE = """
        SELECT id, first_name, last_name, middle_name, birth_date, phone_number, created_at 
        FROM guest WHERE phone_number = :phoneNumber
        """;

    private static final String FIND_ALL = """
        SELECT id, first_name, last_name, middle_name, birth_date, phone_number, created_at 
        FROM guest ORDER BY last_name, first_name
        """;

    private static final String UPDATE = """
        UPDATE guest
        SET first_name = :firstName,
            last_name = :lastName,
            middle_name = :middleName,
            birth_date = :birthDate,
            phone_number = :phoneNumber
        WHERE id = :id
        """;

    private static final String DELETE = """
        DELETE FROM guest WHERE id = :id
        """;

    private static final String EXISTS_BY_PHONE = """
        SELECT COUNT(*) > 0 FROM guest WHERE phone_number = :phoneNumber
        """;

    private static final String EXISTS_BY_ID = """
        SELECT COUNT(*) > 0 FROM guest WHERE id = :id
        """;

    public GuestRepository(NamedParameterJdbcTemplate jdbcTemplate,
                           GuestRowMapper rowMapper) {
        super(jdbcTemplate, TABLE_NAME);
        this.rowMapper = rowMapper;
    }

    public GuestEntity save(GuestEntity guest) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("firstName", guest.firstName())
                .addValue("lastName", guest.lastName())
                .addValue("middleName", guest.middleName())
                .addValue("birthDate", guest.birthDate())
                .addValue("phoneNumber", guest.phoneNumber());

        if (guest.id() == null) {
            // INSERT
            params.addValue("createdAt", LocalDateTime.now());
            Number key = simpleJdbcInsert.executeAndReturnKey(params);
            return new GuestEntity(
                    key.longValue(),
                    guest.firstName(),
                    guest.lastName(),
                    guest.middleName(),
                    guest.birthDate(),
                    guest.phoneNumber(),
                    LocalDateTime.now()
            );
        } else {
            // UPDATE
            params.addValue("id", guest.id());
            jdbcTemplate.update(UPDATE, params);

            return findById(guest.id())
                    .orElseThrow(() -> new RuntimeException("Guest not found after update"));
        }
    }

    public Optional<GuestEntity> findById(Long id) {
        try {
            GuestEntity guest = jdbcTemplate.queryForObject(
                    FIND_BY_ID,
                    Map.of("id", id),
                    rowMapper
            );
            return Optional.ofNullable(guest);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<GuestEntity> findByPhone(String phoneNumber) {
        try {
            GuestEntity guest = jdbcTemplate.queryForObject(
                    FIND_BY_PHONE,
                    Map.of("phoneNumber", phoneNumber),
                    rowMapper
            );
            return Optional.ofNullable(guest);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<GuestEntity> findAll() {
        return jdbcTemplate.query(FIND_ALL, rowMapper);
    }

    public boolean delete(Long id) {
        int affected = jdbcTemplate.update(
                DELETE,
                Map.of("id", id)
        );
        return affected > 0;
    }

    public boolean existsByPhone(String phoneNumber) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                EXISTS_BY_PHONE,
                Map.of("phoneNumber", phoneNumber),
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
