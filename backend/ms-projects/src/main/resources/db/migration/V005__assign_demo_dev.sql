-- Asigna algunas tareas al recurso 'Dev Demo' (id 11 en ms-resources, ver su
-- V003) para que el dashboard del usuario dev@ muestre tareas reales.
-- Referencia logica cross-service por id (sin FK fisica entre BD).

UPDATE tasks SET assignee_resource_id = 11 WHERE id IN (5, 6, 11);
