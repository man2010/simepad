package com.simepad.adapter.out.openmeteo;

import com.fasterxml.jackson.databind.JsonNode;
import com.simepad.domain.model.EnvironmentalDataSnapshot;
import com.simepad.domain.port.out.EnvironmentalDataProvider;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** HTTP adapter for Open-Meteo's separate marine forecast endpoint. */
public final class OpenMeteoMarineProvider implements EnvironmentalDataProvider {
    private final WebClient webClient;

    public OpenMeteoMarineProvider(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public EnvironmentalDataSnapshot fetchLatest(double latitude, double longitude) {
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "wave_height,wave_direction,wave_period,ocean_current_velocity,ocean_current_direction,sea_surface_temperature")
                        .queryParam("timezone", "UTC")
                        .queryParam("cell_selection", "sea")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.hasNonNull("current")) {
            throw new IllegalStateException("Open-Meteo Marine returned no current data");
        }
        JsonNode current = response.get("current");
        Map<String, Double> measurements = new LinkedHashMap<>();
        addIfNumeric(current, measurements, "wave_height", "wave_height_m");
        addIfNumeric(current, measurements, "wave_direction", "wave_direction_degrees");
        addIfNumeric(current, measurements, "wave_period", "wave_period_seconds");
        addIfNumeric(current, measurements, "ocean_current_velocity", "ocean_current_velocity_kmh");
        addIfNumeric(current, measurements, "ocean_current_direction", "ocean_current_direction_degrees");
        addIfNumeric(current, measurements, "sea_surface_temperature", "sea_surface_temperature_c");
        if (measurements.isEmpty() || !current.hasNonNull("time")) {
            throw new IllegalStateException("Open-Meteo Marine response is missing expected measurements");
        }
        Instant observedAt = OffsetDateTime.parse(current.get("time").asText() + "Z").toInstant();
        return new EnvironmentalDataSnapshot("open-meteo-marine", "Dakar", observedAt, measurements);
    }

    private static void addIfNumeric(JsonNode source, Map<String, Double> target, String input, String output) {
        JsonNode value = source.get(input);
        if (value != null && value.isNumber() && Double.isFinite(value.doubleValue())) {
            target.put(output, value.doubleValue());
        }
    }
}
