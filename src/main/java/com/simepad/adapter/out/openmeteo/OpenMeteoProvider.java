package com.simepad.adapter.out.openmeteo;

import com.fasterxml.jackson.databind.JsonNode;
import com.simepad.domain.model.EnvironmentalDataSnapshot;
import com.simepad.domain.port.out.EnvironmentalDataProvider;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** HTTP adapter for the free, non-commercial Open-Meteo forecast API. */
public final class OpenMeteoProvider implements EnvironmentalDataProvider {
    private final WebClient webClient;

    public OpenMeteoProvider(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public EnvironmentalDataSnapshot fetchLatest(double latitude, double longitude) {
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,wind_direction_10m,pressure_msl")
                        .queryParam("timezone", "UTC")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || !response.hasNonNull("current")) {
            throw new IllegalStateException("Open-Meteo returned no current weather data");
        }
        JsonNode current = response.get("current");
        Map<String, Double> measurements = new LinkedHashMap<>();
        addIfNumeric(current, measurements, "temperature_2m", "air_temperature_c");
        addIfNumeric(current, measurements, "relative_humidity_2m", "relative_humidity_percent");
        addIfNumeric(current, measurements, "precipitation", "precipitation_mm");
        addIfNumeric(current, measurements, "wind_speed_10m", "wind_speed_kmh");
        addIfNumeric(current, measurements, "wind_direction_10m", "wind_direction_degrees");
        addIfNumeric(current, measurements, "pressure_msl", "sea_level_pressure_hpa");
        if (measurements.isEmpty() || !current.hasNonNull("time")) {
            throw new IllegalStateException("Open-Meteo response is missing expected current measurements");
        }
        Instant observedAt = OffsetDateTime.parse(current.get("time").asText() + "Z").toInstant();
        return new EnvironmentalDataSnapshot("open-meteo", "Dakar", observedAt, measurements);
    }

    private static void addIfNumeric(JsonNode source, Map<String, Double> target, String input, String output) {
        JsonNode value = source.get(input);
        if (value != null && value.isNumber() && Double.isFinite(value.doubleValue())) {
            target.put(output, value.doubleValue());
        }
    }
}
