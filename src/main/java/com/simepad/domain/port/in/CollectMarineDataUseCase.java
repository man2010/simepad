package com.simepad.domain.port.in;

import com.simepad.domain.model.EnvironmentalObservation;

public interface CollectMarineDataUseCase {
    EnvironmentalObservation collect(double latitude, double longitude);
}
