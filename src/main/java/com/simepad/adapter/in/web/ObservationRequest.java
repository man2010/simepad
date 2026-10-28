package com.simepad.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public record ObservationRequest(
        @NotBlank String source,
        @NotBlank String location,
        @NotNull Instant observedAt,
        @NotEmpty Map<String, Double> measurements) {
}
