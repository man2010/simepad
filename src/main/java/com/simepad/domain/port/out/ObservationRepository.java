package com.simepad.domain.port.out;

import com.simepad.domain.model.EnvironmentalObservation;

import java.time.Instant;
import java.util.List;

public interface ObservationRepository {
    EnvironmentalObservation save(EnvironmentalObservation observation);

    List<EnvironmentalObservation> findByFilters(String source, Instant from, Instant to, int limit);
}
