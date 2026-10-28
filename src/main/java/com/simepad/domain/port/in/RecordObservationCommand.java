package com.simepad.domain.port.in;

import java.time.Instant;
import java.util.Map;

public record RecordObservationCommand(
        String source,
        String location,
        Instant observedAt,
        Map<String, Double> measurements) {
}
