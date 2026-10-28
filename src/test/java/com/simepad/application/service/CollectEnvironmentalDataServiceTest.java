package com.simepad.application.service;

import com.simepad.domain.model.EnvironmentalDataSnapshot;
import com.simepad.domain.model.EnvironmentalObservation;
import com.simepad.domain.port.in.RecordObservationUseCase;
import com.simepad.domain.port.out.EnvironmentalDataProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CollectEnvironmentalDataServiceTest {
    @Test
    void fetchesSnapshotAndRecordsItThroughTheUseCasePort() {
        EnvironmentalDataProvider provider = mock(EnvironmentalDataProvider.class);
        RecordObservationUseCase recorder = mock(RecordObservationUseCase.class);
        var snapshot = new EnvironmentalDataSnapshot("open-meteo", "Dakar",
                Instant.parse("2026-10-28T10:00:00Z"), Map.of("wind_speed_kmh", 14.0));
        var saved = new EnvironmentalObservation(java.util.UUID.randomUUID(), "open-meteo", "Dakar",
                snapshot.observedAt(), snapshot.measurements());
        when(provider.fetchLatest(14.69, -17.44)).thenReturn(snapshot);
        when(recorder.record(org.mockito.ArgumentMatchers.any())).thenReturn(saved);
        var service = new CollectEnvironmentalDataService(provider, recorder);

        var result = service.collect(14.69, -17.44);

        assertEquals(saved, result);
        verify(provider).fetchLatest(14.69, -17.44);
        verify(recorder).record(new com.simepad.domain.port.in.RecordObservationCommand(
                "open-meteo", "Dakar", snapshot.observedAt(), snapshot.measurements()));
    }

    @Test
    void rejectsInvalidCoordinatesBeforeCallingProvider() {
        EnvironmentalDataProvider provider = mock(EnvironmentalDataProvider.class);
        RecordObservationUseCase recorder = mock(RecordObservationUseCase.class);
        var service = new CollectEnvironmentalDataService(provider, recorder);

        assertThrows(IllegalArgumentException.class, () -> service.collect(91, 0));
        verifyNoInteractions(provider, recorder);
    }
}
