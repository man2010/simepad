CREATE TABLE environmental_observations (
    id UUID PRIMARY KEY,
    source VARCHAR(120) NOT NULL,
    location VARCHAR(200) NOT NULL,
    observed_at TIMESTAMPTZ NOT NULL,
    measurements JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT environmental_observations_measurements_object
        CHECK (jsonb_typeof(measurements) = 'object' AND measurements <> '{}'::jsonb)
);

CREATE INDEX idx_environmental_observations_observed_at
    ON environmental_observations (observed_at DESC);

CREATE INDEX idx_environmental_observations_source_observed_at
    ON environmental_observations (source, observed_at DESC);
