package com.simepad.adapter.in.web;

import com.simepad.domain.port.in.RecordObservationCommand;
import com.simepad.domain.port.in.RecordObservationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/observations")
public class ObservationController {
    private final RecordObservationUseCase recordObservation;

    public ObservationController(RecordObservationUseCase recordObservation) {
        this.recordObservation = recordObservation;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ObservationResponse record(@Valid @RequestBody ObservationRequest request) {
        var command = new RecordObservationCommand(
                request.source(), request.location(), request.observedAt(), request.measurements());
        return ObservationResponse.from(recordObservation.record(command));
    }

    @GetMapping
    public List<ObservationResponse> find(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "100") int limit) {
        return recordObservation.find(source, from, to, limit).stream()
                .map(ObservationResponse::from)
                .toList();
    }
}
