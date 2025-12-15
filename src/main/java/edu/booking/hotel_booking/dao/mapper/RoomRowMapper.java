package edu.booking.hotel_booking.dao.mapper;

import edu.booking.hotel_booking.entity.RoomEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RoomRowMapper implements RowMapper<RoomEntity> {
    @Override
    public RoomEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RoomEntity(
                rs.getLong("id"),
                rs.getInt("floor"),
                rs.getString("room_number"),
                rs.getInt("capacity"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
