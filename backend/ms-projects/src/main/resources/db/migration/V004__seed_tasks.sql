-- Dev seed: tasks across the two seeded projects, assigned to the seeded
-- resources (ids 1-10). Mixed statuses and a few overdue (due_date in the
-- past and not DONE) so analytics KPIs (delayedTasks, utilization) are real.

INSERT INTO tasks (project_id, title, description, status, assignee_resource_id, estimated_hours, due_date) VALUES
    (1, 'Diseno de arquitectura de microservicios', 'Definir limites de servicios y contratos.', 'DONE',        5, 10, DATE '2026-02-15'),
    (1, 'Implementar API Gateway',                  'Ruteo y seguridad JWT.',                    'DONE',        4, 12, DATE '2026-03-01'),
    (1, 'Modulo de gestion de proyectos',           'CRUD de proyectos y estados.',              'IN_PROGRESS', 1, 16, DATE '2026-07-10'),
    (1, 'Modulo de recursos',                       'CRUD de recursos y disponibilidad.',        'IN_PROGRESS', 2, 14, DATE '2026-07-20'),
    (1, 'Panel de analitica (KPIs)',                'Dashboard de indicadores para directivos.', 'TODO',        3, 18, DATE '2026-08-05'),
    (1, 'Pruebas unitarias backend',                'Cobertura minima 60%.',                     'TODO',        7, 12, DATE '2026-08-20'),
    (1, 'Pipeline CI/CD',                           'Build y tests automaticos.',                'IN_PROGRESS',10, 8, DATE '2026-06-10'),
    (1, 'Documentacion tecnica',                    'Diagramas C1/C2/C3 e informe.',             'BLOCKED',     9,  6, DATE '2026-06-05'),
    (2, 'Levantamiento de requerimientos',          'Relevamiento con el cliente Acme.',         'DONE',        6, 10, DATE '2026-06-12'),
    (2, 'Diseno de migracion cloud',                'Plan de migracion de infraestructura.',     'IN_PROGRESS', 6, 15, DATE '2026-07-15'),
    (2, 'Migracion de base de datos',               'Mover datos a la nube.',                    'TODO',       10, 20, DATE '2026-08-01'),
    (2, 'Refactor de servicios legacy',             'Adaptar servicios al nuevo entorno.',       'IN_PROGRESS', 1, 16, DATE '2026-06-08'),
    (2, 'Pruebas de integracion',                   'Validar flujos end-to-end.',                'TODO',        8, 12, DATE '2026-08-25'),
    (2, 'Diseno UX del portal cliente',             'Wireframes y prototipo.',                   'IN_PROGRESS', 9, 10, DATE '2026-07-05'),
    (2, 'Capacitacion del equipo Acme',             'Onboarding tecnico.',                       'TODO',        5,  6, DATE '2026-09-01'),
    (2, 'Hardening de seguridad',                   'Revision de vulnerabilidades.',             'BLOCKED',     4,  8, DATE '2026-06-01');
