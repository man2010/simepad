package com.simepad.adapter.out.persistence.memory;

import com.simepad.domain.model.EnvironmentalObservation;
import com.simepad.domain.port.out.ObservationRepository;

import java.util.UUID;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** Temporary adapter used until the database adapter is introduced. */
public final class InMemoryObservationRepository implements ObservationRepository {
    private final ConcurrentHashMap<UUID, EnvironmentalObservation> observations = new ConcurrentHashMap<>();

    @Override
    public EnvironmentalObservation save(EnvironmentalObservation observation) {
        observations.put(observation.id(), observation);
        return observation;
    }

    @Override
    public List<EnvironmentalObservation> findByFilters(String source, Instant from, Instant to, int limit) {
        return observations.values().stream()
                .filter(o -> source == null || source.isBlank() || o.source().equalsIgnoreCase(source))
                .filter(o -> from == null || !o.observedAt().isBefore(from))
                .filter(o -> to == null || !o.observedAt().isAfter(to))
                .sorted(Comparator.comparing(EnvironmentalObservation::observedAt).reversed())
                .limit(limit)
                .toList();
    }
}
