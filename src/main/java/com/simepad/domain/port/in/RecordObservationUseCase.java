package com.simepad.domain.port.in;

import com.simepad.domain.model.EnvironmentalObservation;

import java.time.Instant;
import java.util.List;

public interface RecordObservationUseCase {
    EnvironmentalObservation record(RecordObservationCommand command);

    List<EnvironmentalObservation> find(String source, Instant from, Instant to, int limit);
}
