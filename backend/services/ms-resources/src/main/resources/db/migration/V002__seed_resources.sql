-- Dev seed: ten resources mixing roles, capacities and skill sets.
-- Used by manual GET checks and by the BFF dashboards when integration
-- with ms-analitica lands.

INSERT INTO resources (name, email, role, weekly_hours, skills, active) VALUES
    -- Devs
    ('Maria Fernandez',  'maria.fernandez@innovatech.cl',  'DEV',      40, 'java,spring,postgres',            TRUE),
    ('Diego Soto',       'diego.soto@innovatech.cl',       'DEV',      40, 'java,kafka,redis',                TRUE),
    ('Camila Rojas',     'camila.rojas@innovatech.cl',     'DEV',      20, 'react,typescript,tailwind',       TRUE),
    ('Ignacio Vergara',  'ignacio.vergara@innovatech.cl',  'DEV',      40, 'python,fastapi,postgres',         TRUE),
    -- PMs
    ('Valentina Pinto',  'valentina.pinto@innovatech.cl',  'PM',       40, 'scrum,jira,stakeholder-mgmt',     TRUE),
    ('Andres Munoz',     'andres.munoz@innovatech.cl',     'PM',       30, 'kanban,roadmap,okr',              TRUE),
    -- QA
    ('Paula Castillo',   'paula.castillo@innovatech.cl',   'QA',       40, 'cypress,playwright,api-testing',  TRUE),
    ('Rodrigo Tapia',    'rodrigo.tapia@innovatech.cl',    'QA',       20, 'selenium,jmeter,exploratory',     TRUE),
    -- Designer
    ('Sofia Aguilar',    'sofia.aguilar@innovatech.cl',    'DESIGNER', 40, 'figma,ux-research,prototyping',   TRUE),
    -- DevOps
    ('Joaquin Henriquez','joaquin.henriquez@innovatech.cl','DEVOPS',   40, 'docker,kubernetes,terraform',     TRUE);
