package edu.booking.hotel_booking.dao;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.beans.FeatureDescriptor;
import java.util.Arrays;
import java.util.stream.Collectors;

@Repository
public abstract class BaseRepository {

    protected final NamedParameterJdbcTemplate jdbcTemplate;
    protected final SimpleJdbcInsert simpleJdbcInsert;

    protected BaseRepository(NamedParameterJdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName(tableName)
                .usingGeneratedKeyColumns("id");
    }

//    protected MapSqlParameterSource toParamSource(Object object) {
//        return new MapSqlParameterSource(
//                Arrays.stream(new BeanWrapperImpl(object).getPropertyDescriptors())
//                        .filter(pd -> pd.getReadMethod() != null)
//                        .collect(Collectors.toMap(
//                                FeatureDescriptor::getName,
//                                pd -> {
//                                    try {
//                                        return pd.getReadMethod().invoke(object);
//                                    } catch (Exception e) {
//                                        return null;
//                                    }
//                                }
//                        ))
//        );
//    }
}
