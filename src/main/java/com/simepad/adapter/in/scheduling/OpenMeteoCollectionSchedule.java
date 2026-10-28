package com.simepad.adapter.in.scheduling;

import com.simepad.domain.port.in.CollectEnvironmentalDataUseCase;
import com.simepad.domain.port.in.CollectMarineDataUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically refreshes both Open-Meteo sources when scheduling is enabled. */
@Component
@ConditionalOnProperty(name = "simepad.collection.schedule.enabled", havingValue = "true")
public class OpenMeteoCollectionSchedule {
    private static final Logger log = LoggerFactory.getLogger(OpenMeteoCollectionSchedule.class);
    private static final double DAKAR_LATITUDE = 14.6928;
    private static final double DAKAR_LONGITUDE = -17.4467;

    private final CollectEnvironmentalDataUseCase collectWeather;
    private final CollectMarineDataUseCase collectMarine;

    public OpenMeteoCollectionSchedule(CollectEnvironmentalDataUseCase collectWeather,
                                      CollectMarineDataUseCase collectMarine) {
        this.collectWeather = collectWeather;
        this.collectMarine = collectMarine;
    }

    @Scheduled(
            initialDelayString = "${simepad.collection.schedule.initial-delay-ms:3600000}",
            fixedDelayString = "${simepad.collection.schedule.fixed-delay-ms:3600000}")
    public void collect() {
        collectSafely("weather", () -> collectWeather.collect(DAKAR_LATITUDE, DAKAR_LONGITUDE));
        collectSafely("marine", () -> collectMarine.collect(DAKAR_LATITUDE, DAKAR_LONGITUDE));
    }

    private void collectSafely(String source, RunnableCollection collection) {
        try {
            collection.run();
            log.info("Scheduled {} observation collected for Dakar", source);
        } catch (Exception exception) {
            log.warn("Scheduled {} collection failed: {}", source, exception.getMessage());
        }
    }

    @FunctionalInterface
    private interface RunnableCollection {
        Object run();
    }
}
