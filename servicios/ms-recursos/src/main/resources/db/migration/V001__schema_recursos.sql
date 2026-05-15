-- Initial schema for ms-recursos.
-- Mirrors cl.duoc.innovatech.recursos.entity.Recurso.

CREATE TABLE recursos (
    id              BIGSERIAL    PRIMARY KEY,
    nombre          VARCHAR(200) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    rol             VARCHAR(30)  NOT NULL,
    horas_semanales INTEGER      NOT NULL,
    competencias    VARCHAR(500),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Email is the natural functional key; index supports both the UNIQUE
-- constraint check on insert and the future "lookup by email" use case.
CREATE INDEX idx_recursos_rol ON recursos (rol);
