-- V1__schema.sql: Initial schema for Multicloud AI Workload Optimization System

CREATE TABLE cloud_provider (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE instance_type (
    id SERIAL PRIMARY KEY,
    provider_id INT NOT NULL REFERENCES cloud_provider(id) ON DELETE CASCADE,
    instance_name VARCHAR(64) NOT NULL UNIQUE,
    gpu_model VARCHAR(32) NOT NULL,
    gpu_count INT NOT NULL,
    vcpu INT NOT NULL DEFAULT 8,
    memory_gb INT NOT NULL DEFAULT 32
);

CREATE TABLE price (
    id SERIAL PRIMARY KEY,
    instance_type_id INT NOT NULL REFERENCES instance_type(id) ON DELETE CASCADE,
    region VARCHAR(64) NOT NULL,
    pricing_model VARCHAR(16) NOT NULL, -- ON_DEMAND or SPOT
    hourly_price_usd NUMERIC(12, 4) NOT NULL,
    interruption_rate_pct NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    avg_queue_minutes INT NOT NULL DEFAULT 5,
    as_of_date DATE NOT NULL,
    CONSTRAINT uq_instance_region_model UNIQUE (instance_type_id, region, pricing_model)
);

CREATE INDEX idx_price_lookup ON price (instance_type_id, region, pricing_model);
CREATE INDEX idx_price_gpu_model ON instance_type (gpu_model);

CREATE TABLE egress_rate (
    id SERIAL PRIMARY KEY,
    from_provider VARCHAR(32) NOT NULL,
    from_region VARCHAR(64) NOT NULL,
    to_provider VARCHAR(32) NOT NULL,
    to_region VARCHAR(64) NOT NULL,
    usd_per_gb NUMERIC(12, 4) NOT NULL,
    CONSTRAINT uq_egress_route UNIQUE (from_provider, from_region, to_provider, to_region)
);

CREATE INDEX idx_egress_lookup ON egress_rate (from_provider, from_region, to_provider, to_region);

CREATE TABLE job (
    id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    gpu_model VARCHAR(32) NOT NULL,
    gpu_count INT NOT NULL,
    duration_hours NUMERIC(8, 2) NOT NULL,
    data_gb NUMERIC(10, 2) NOT NULL,
    data_provider VARCHAR(32) NOT NULL,
    data_region VARCHAR(64) NOT NULL,
    deadline_hours NUMERIC(8, 2) NOT NULL,
    spot_allowed BOOLEAN NOT NULL DEFAULT TRUE,
    checkpointing BOOLEAN NOT NULL DEFAULT TRUE,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE placement (
    id SERIAL PRIMARY KEY,
    job_id INT NOT NULL REFERENCES job(id) ON DELETE CASCADE,
    instance_type_id INT NOT NULL REFERENCES instance_type(id) ON DELETE CASCADE,
    region VARCHAR(64) NOT NULL,
    pricing_model VARCHAR(16) NOT NULL,
    compute_cost NUMERIC(12, 4) NOT NULL,
    egress_cost NUMERIC(12, 4) NOT NULL,
    interruption_cost NUMERIC(12, 4) NOT NULL,
    total_cost NUMERIC(12, 4) NOT NULL,
    est_finish_hours NUMERIC(8, 2) NOT NULL,
    strategy VARCHAR(32) NOT NULL, -- GREEDY, ILP, ALL_AWS, ALL_GCP, ALL_AZURE
    rank INT NOT NULL DEFAULT 1,
    explanation TEXT
);

CREATE INDEX idx_placement_job ON placement (job_id);
