package com.simepad.adapter.out.persistence.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simepad.domain.model.EnvironmentalObservation;
import com.simepad.domain.port.out.ObservationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** PostgreSQL adapter. SQL and JSON serialization stay outside the domain. */
public final class PostgresObservationRepository implements ObservationRepository {
    private static final TypeReference<Map<String, Double>> MEASUREMENTS_TYPE = new TypeReference<>() { };

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PostgresObservationRepository(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new ObjectMapper().findAndRegisterModules());
    }

    PostgresObservationRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public EnvironmentalObservation save(EnvironmentalObservation observation) {
        String sql = """
                INSERT INTO environmental_observations
                    (id, source, location, observed_at, measurements)
                VALUES (?, ?, ?, ?, CAST(? AS jsonb))
                """;
        jdbcTemplate.update(sql,
                observation.id(), observation.source(), observation.location(),
                Timestamp.from(observation.observedAt()), writeMeasurements(observation.measurements()));
        return observation;
    }

    @Override
    public List<EnvironmentalObservation> findByFilters(String source, Instant from, Instant to, int limit) {
        String sql = """
                SELECT id, source, location, observed_at, measurements::text AS measurements
                FROM environmental_observations
                WHERE (? IS NULL OR lower(source) = lower(?))
                  AND (? IS NULL OR observed_at >= ?)
                  AND (? IS NULL OR observed_at <= ?)
                ORDER BY observed_at DESC
                LIMIT ?
                """;
        Timestamp fromTimestamp = from == null ? null : Timestamp.from(from);
        Timestamp toTimestamp = to == null ? null : Timestamp.from(to);
        return jdbcTemplate.query(sql, ROW_MAPPER,
                blankToNull(source), blankToNull(source),
                fromTimestamp, fromTimestamp,
                toTimestamp, toTimestamp,
                limit);
    }

    private String writeMeasurements(Map<String, Double> measurements) {
        try {
            return objectMapper.writeValueAsString(measurements);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize observation measurements", exception);
        }
    }

    private Map<String, Double> readMeasurements(String json) {
        try {
            return objectMapper.readValue(json, MEASUREMENTS_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not deserialize observation measurements", exception);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private final RowMapper<EnvironmentalObservation> ROW_MAPPER = (resultSet, rowNumber) ->
            new EnvironmentalObservation(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getString("source"),
                    resultSet.getString("location"),
                    resultSet.getTimestamp("observed_at").toInstant(),
                    readMeasurements(resultSet.getString("measurements")));
}
