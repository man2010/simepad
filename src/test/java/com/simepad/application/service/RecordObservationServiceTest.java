package com.simepad.application.service;

import com.simepad.domain.model.EnvironmentalObservation;
import com.simepad.domain.port.in.RecordObservationCommand;
import com.simepad.domain.port.out.ObservationRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordObservationServiceTest {
    @Test
    void recordsNormalizedObservationThroughOutputPort() {
        ObservationRepository repository = mock(ObservationRepository.class);
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(call -> call.getArgument(0));
        var service = new RecordObservationService(repository);
        var command = new RecordObservationCommand("open-meteo", "Dakar", Instant.parse("2026-10-28T10:00:00Z"),
                Map.of("wind_speed", 12.5));

        EnvironmentalObservation saved = service.record(command);

        assertEquals("open-meteo", saved.source());
        assertEquals(12.5, saved.measurements().get("wind_speed"));
        verify(repository).save(saved);
    }

    @Test
    void rejectsNonFiniteMeasurementValues() {
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentalObservation(
                java.util.UUID.randomUUID(), "test", "Dakar", Instant.now(), Map.of("wind", Double.NaN)));
    }

    @Test
    void rejectsInvalidSearchLimit() {
        var service = new RecordObservationService(mock(ObservationRepository.class));
        assertThrows(IllegalArgumentException.class, () -> service.find(null, null, null, 501));
    }
}
