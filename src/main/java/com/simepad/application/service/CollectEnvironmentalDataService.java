package com.simepad.application.service;

import com.simepad.domain.model.EnvironmentalObservation;
import com.simepad.domain.port.in.CollectEnvironmentalDataUseCase;
import com.simepad.domain.port.in.RecordObservationCommand;
import com.simepad.domain.port.in.RecordObservationUseCase;
import com.simepad.domain.port.out.EnvironmentalDataProvider;

import java.util.Objects;

public final class CollectEnvironmentalDataService implements CollectEnvironmentalDataUseCase {
    private final EnvironmentalDataProvider provider;
    private final RecordObservationUseCase recordObservation;

    public CollectEnvironmentalDataService(EnvironmentalDataProvider provider,
                                           RecordObservationUseCase recordObservation) {
        this.provider = Objects.requireNonNull(provider);
        this.recordObservation = Objects.requireNonNull(recordObservation);
    }

    @Override
    public EnvironmentalObservation collect(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordinates must be valid latitude and longitude values");
        }
        var snapshot = provider.fetchLatest(latitude, longitude);
        return recordObservation.record(new RecordObservationCommand(
                snapshot.source(), snapshot.location(), snapshot.observedAt(), snapshot.measurements()));
    }
}
