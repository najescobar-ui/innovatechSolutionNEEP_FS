-- Initial schema for ms-recursos.
-- Mirrors cl.duoc.innovatech.resources.entity.Resource.

CREATE TABLE resources (
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    role            VARCHAR(30)  NOT NULL,
    weekly_hours    INTEGER      NOT NULL,
    skills          VARCHAR(500),
    active          BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Email is the natural functional key; index supports both the UNIQUE
-- constraint check on insert and the future "lookup by email" use case.
CREATE INDEX idx_resources_role ON resources (role);
