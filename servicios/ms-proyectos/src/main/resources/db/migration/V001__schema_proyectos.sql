-- Initial schema for ms-proyectos.
-- Mirrors cl.duoc.innovatech.proyectos.entity.Proyecto.

CREATE TABLE proyectos (
    id                     BIGSERIAL    PRIMARY KEY,
    nombre                 VARCHAR(200) NOT NULL,
    descripcion            VARCHAR(1000),
    estado                 VARCHAR(30)  NOT NULL,
    fecha_inicio           DATE,
    fecha_fin_planificada  DATE,
    responsable_id         VARCHAR(64)
);
