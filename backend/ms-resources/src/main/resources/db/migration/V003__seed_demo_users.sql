-- Recursos que corresponden a los usuarios de demo de Keycloak (dev@, pm@),
-- para que el dashboard resuelva usuario -> recurso por email y muestre datos
-- reales. El orden importa: 'dev' queda con id 11 y 'pm' con id 12.

INSERT INTO resources (name, email, role, weekly_hours, skills, active) VALUES
    ('Dev Demo', 'dev@innovatech.cl', 'DEV', 40, 'java,spring,react', TRUE),
    ('PM Demo',  'pm@innovatech.cl',  'PM',  40, 'scrum,roadmap',      TRUE);
