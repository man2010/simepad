package com.simepad.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** A normalized snapshot returned by an external data source. */
public record EnvironmentalDataSnapshot(
        String source,
        String location,
        Instant observedAt,
        Map<String, Double> measurements) {

    public EnvironmentalDataSnapshot {
        Objects.requireNonNull(source, "source is required");
        Objects.requireNonNull(location, "location is required");
        Objects.requireNonNull(observedAt, "observedAt is required");
        measurements = Map.copyOf(Objects.requireNonNull(measurements, "measurements are required"));
        if (source.isBlank() || location.isBlank() || measurements.isEmpty()) {
            throw new IllegalArgumentException("Snapshot source, location and measurements are required");
        }
        measurements.forEach((name, value) -> {
            if (name == null || name.isBlank() || value == null || !Double.isFinite(value)) {
                throw new IllegalArgumentException("Snapshot measurements must have names and finite values");
            }
        });
    }
}
