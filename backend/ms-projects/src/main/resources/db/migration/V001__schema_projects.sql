-- Initial schema for ms-projects.
-- Mirrors cl.duoc.innovatech.projects.entity.Project.

CREATE TABLE projects (
    id                  BIGSERIAL    PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    description         VARCHAR(1000),
    status              VARCHAR(30)  NOT NULL,
    start_date          DATE,
    planned_end_date    DATE,
    owner_id            VARCHAR(64)
);
