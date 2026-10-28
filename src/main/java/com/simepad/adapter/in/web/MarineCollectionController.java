package com.simepad.adapter.in.web;

import com.simepad.domain.port.in.CollectMarineDataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/collections")
public class MarineCollectionController {
    private final CollectMarineDataUseCase collectMarineData;

    public MarineCollectionController(CollectMarineDataUseCase collectMarineData) {
        this.collectMarineData = collectMarineData;
    }

    @PostMapping("/open-meteo-marine")
    @ResponseStatus(HttpStatus.CREATED)
    public ObservationResponse collectMarine(
            @RequestParam(defaultValue = "14.6928") double latitude,
            @RequestParam(defaultValue = "-17.4467") double longitude) {
        return ObservationResponse.from(collectMarineData.collect(latitude, longitude));
    }
}
