package com.simepad.domain.port.out;

import com.simepad.domain.model.EnvironmentalDataSnapshot;

/** Outbound port implemented by Open-Meteo, Copernicus, simulators, etc. */
public interface EnvironmentalDataProvider {
    EnvironmentalDataSnapshot fetchLatest(double latitude, double longitude);
}
