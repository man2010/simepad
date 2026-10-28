package com.simepad.adapter.in.web;

import com.simepad.domain.model.EnvironmentalObservation;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ObservationResponse(
        UUID id,
        String source,
        String location,
        Instant observedAt,
        Map<String, Double> measurements) {

    public static ObservationResponse from(EnvironmentalObservation observation) {
        return new ObservationResponse(observation.id(), observation.source(), observation.location(),
                observation.observedAt(), observation.measurements());
    }
}
