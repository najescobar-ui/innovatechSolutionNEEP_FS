-- Initial schema for the scaffolded microservice.
-- Rename the table/columns to match your domain, or replace this sample entity.
CREATE TABLE samples (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);
