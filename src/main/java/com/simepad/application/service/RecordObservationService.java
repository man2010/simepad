package com.simepad.application.service;

import com.simepad.domain.model.EnvironmentalObservation;
import com.simepad.domain.port.in.RecordObservationCommand;
import com.simepad.domain.port.in.RecordObservationUseCase;
import com.simepad.domain.port.out.ObservationRepository;

import java.util.Objects;
import java.util.UUID;
import java.time.Instant;
import java.util.List;

/** Application service: coordinates the use case without depending on a framework. */
public final class RecordObservationService implements RecordObservationUseCase {
    private final ObservationRepository repository;

    public RecordObservationService(ObservationRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public EnvironmentalObservation record(RecordObservationCommand command) {
        Objects.requireNonNull(command, "command is required");
        var observation = new EnvironmentalObservation(
                UUID.randomUUID(), command.source(), command.location(),
                command.observedAt(), command.measurements());
        return repository.save(observation);
    }

    @Override
    public List<EnvironmentalObservation> find(String source, Instant from, Instant to, int limit) {
        if (limit < 1 || limit > 500) {
            throw new IllegalArgumentException("limit must be between 1 and 500");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be earlier than or equal to to");
        }
        return repository.findByFilters(source, from, to, limit);
    }
}
