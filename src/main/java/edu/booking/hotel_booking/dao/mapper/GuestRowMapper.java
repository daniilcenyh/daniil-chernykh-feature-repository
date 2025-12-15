package edu.booking.hotel_booking.dao.mapper;

import edu.booking.hotel_booking.entity.GuestEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GuestRowMapper implements RowMapper<GuestEntity> {

    @Override
    public GuestEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new GuestEntity(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("middle_name"),
                rs.getDate("birth_date").toLocalDate(),
                rs.getString("phone_number"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
