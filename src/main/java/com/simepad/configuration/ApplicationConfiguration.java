package com.simepad.configuration;

import com.simepad.adapter.out.persistence.postgres.PostgresObservationRepository;
import com.simepad.application.service.RecordObservationService;
import com.simepad.application.service.CollectEnvironmentalDataService;
import com.simepad.adapter.out.openmeteo.OpenMeteoProvider;
import com.simepad.adapter.out.openmeteo.OpenMeteoMarineProvider;
import com.simepad.domain.port.in.CollectEnvironmentalDataUseCase;
import com.simepad.domain.port.in.CollectMarineDataUseCase;
import com.simepad.domain.port.out.EnvironmentalDataProvider;
import com.simepad.application.service.CollectMarineDataService;
import com.simepad.domain.port.in.RecordObservationUseCase;
import com.simepad.domain.port.out.ObservationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfiguration {
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/**", "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .build();
    }

    @Bean
    ObservationRepository observationRepository(JdbcTemplate jdbcTemplate) {
        return new PostgresObservationRepository(jdbcTemplate);
    }

    @Bean
    RecordObservationUseCase recordObservationUseCase(ObservationRepository repository) {
        return new RecordObservationService(repository);
    }

    @Bean
    WebClient openMeteoWebClient(WebClient.Builder builder,
                                 org.springframework.core.env.Environment environment) {
        return builder.baseUrl(environment.getProperty(
                "simepad.datasources.open-meteo.base-url", "https://api.open-meteo.com/v1/forecast")).build();
    }

    @Bean
    WebClient openMeteoMarineWebClient(WebClient.Builder builder,
                                       org.springframework.core.env.Environment environment) {
        return builder.baseUrl(environment.getProperty(
                "simepad.datasources.open-meteo-marine.base-url", "https://marine-api.open-meteo.com/v1/marine")).build();
    }

    @Bean
    EnvironmentalDataProvider environmentalDataProvider(WebClient openMeteoWebClient) {
        return new OpenMeteoProvider(openMeteoWebClient);
    }

    @Bean
    CollectEnvironmentalDataUseCase collectEnvironmentalDataUseCase(
            @org.springframework.beans.factory.annotation.Qualifier("environmentalDataProvider")
            EnvironmentalDataProvider provider,
            RecordObservationUseCase recordObservation) {
        return new CollectEnvironmentalDataService(provider, recordObservation);
    }

    @Bean("marineEnvironmentalDataProvider")
    EnvironmentalDataProvider marineEnvironmentalDataProvider(WebClient openMeteoMarineWebClient) {
        return new OpenMeteoMarineProvider(openMeteoMarineWebClient);
    }

    @Bean
    CollectMarineDataUseCase collectMarineDataUseCase(
            @org.springframework.beans.factory.annotation.Qualifier("marineEnvironmentalDataProvider")
            EnvironmentalDataProvider provider,
            RecordObservationUseCase recordObservation) {
        return new CollectMarineDataService(provider, recordObservation);
    }
}
