-- Dev seed: two sample projects, mirrors the legacy bootstrap/SeedData.java.
-- Safe to keep in V002 because the next environment that boots from scratch
-- expects these rows for the demo flows (BFF dashboards, manual GET checks).

INSERT INTO proyectos (nombre, descripcion, estado, fecha_inicio, fecha_fin_planificada, responsable_id) VALUES
    ('Plataforma Innovatech',
     'Sistema de gestion interno de proyectos.',
     'EN_CURSO',
     DATE '2026-01-15',
     DATE '2026-12-01',
     'user-pm-test'),
    ('Migracion Acme',
     'Migracion del cliente Acme a infraestructura cloud.',
     'PLANIFICACION',
     DATE '2026-06-01',
     DATE '2026-10-15',
     'user-pm-test');
