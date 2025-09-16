CREATE TABLE cargo_schedule (
    id UUID PRIMARY KEY,
    cargo_id UUID,
    pickup_time TIMESTAMP,
    criticality VARCHAR(10),
    pickup_location TEXT,
    dropoff_location TEXT,
    description TEXT,
    weight NUMERIC(6, 2)
);
