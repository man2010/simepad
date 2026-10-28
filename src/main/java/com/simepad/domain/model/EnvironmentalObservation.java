package com.simepad.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** A normalized measurement received from any environmental data source. */
public record EnvironmentalObservation(
        UUID id,
        String source,
        String location,
        Instant observedAt,
        Map<String, Double> measurements) {

    public EnvironmentalObservation {
        Objects.requireNonNull(id, "id is required");
        source = requireText(source, "source");
        location = requireText(location, "location");
        Objects.requireNonNull(observedAt, "observedAt is required");
        Objects.requireNonNull(measurements, "measurements are required");
        if (measurements.isEmpty()) {
            throw new IllegalArgumentException("At least one measurement is required");
        }
        measurements = Map.copyOf(measurements);
        measurements.forEach((name, value) -> {
            requireText(name, "measurement name");
            Objects.requireNonNull(value, "measurement value is required");
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Measurement values must be finite");
            }
        });
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
