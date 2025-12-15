package edu.booking.hotel_booking.dao.mapper;

import edu.booking.hotel_booking.entity.BookingEntity;
import edu.booking.hotel_booking.entity.enums.BookingStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class BookingRowMapper implements RowMapper<BookingEntity> {
    @Override
    public BookingEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new BookingEntity(
                rs.getLong("id"),
                rs.getLong("room_id"),
                rs.getTimestamp("start_date").toLocalDateTime(),
                rs.getTimestamp("end_date").toLocalDateTime(),
                rs.getObject("status", BookingStatus.class),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
