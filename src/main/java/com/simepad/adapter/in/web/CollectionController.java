package com.simepad.adapter.in.web;

import com.simepad.domain.port.in.CollectEnvironmentalDataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {
    private final CollectEnvironmentalDataUseCase collectEnvironmentalData;

    public CollectionController(CollectEnvironmentalDataUseCase collectEnvironmentalData) {
        this.collectEnvironmentalData = collectEnvironmentalData;
    }

    @PostMapping("/open-meteo")
    @ResponseStatus(HttpStatus.CREATED)
    public ObservationResponse collectOpenMeteo(
            @RequestParam(defaultValue = "14.6928") double latitude,
            @RequestParam(defaultValue = "-17.4467") double longitude) {
        return ObservationResponse.from(collectEnvironmentalData.collect(latitude, longitude));
    }
}
