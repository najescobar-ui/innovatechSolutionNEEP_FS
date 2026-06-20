-- Tasks of a project. Mirrors cl.duoc.innovatech.projects.entity.Task.
-- assignee_resource_id is a logical reference to ms-resources (no physical
-- cross-service FK), same convention as projects.owner_id.

CREATE TABLE tasks (
    id                      BIGSERIAL    PRIMARY KEY,
    project_id              BIGINT       NOT NULL REFERENCES projects (id),
    title                   VARCHAR(200) NOT NULL,
    description             VARCHAR(1000),
    status                  VARCHAR(30)  NOT NULL,
    assignee_resource_id    BIGINT,
    estimated_hours         INT          NOT NULL DEFAULT 0,
    due_date                DATE
);

CREATE INDEX idx_tasks_project   ON tasks (project_id);
CREATE INDEX idx_tasks_assignee  ON tasks (assignee_resource_id);
CREATE INDEX idx_tasks_status    ON tasks (status);
