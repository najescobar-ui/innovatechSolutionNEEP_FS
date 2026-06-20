# API REST — Innovatech Solutions

Especificación de la API y colección de pruebas (DSY1106).

## Archivos
- `InnovatechSolutions.postman_collection.json` — colección Postman con el flujo completo
  (registro, login, dashboard, CRUD de proyectos/recursos/tareas, KPIs) e **ejemplos de
  petición y respuesta** para cada endpoint.
- `openapi-bff.json` — especificación OpenAPI 3.1 (Swagger) del BFF, la API pública.
- `openapi-ms-projects.json`, `openapi-ms-resources.json`, `openapi-ms-analytics.json` —
  OpenAPI de cada microservicio.

## Cómo probar con Postman
1. Levantar el stack: `kubectl apply -k backend/k8s/overlays/local` (o `docker compose up -d`).
2. Importar `InnovatechSolutions.postman_collection.json` en Postman.
3. Ejecutar **Autenticación → Login (obtener token)**: hace el *password grant* contra
   Keycloak y guarda el `access_token` en la variable `token` de la colección.
4. Ejecutar el resto de las peticiones: el JWT se inyecta solo en la cabecera `Authorization`.

Variables de la colección:
- `base_url` = `http://localhost:9000/api` (API Gateway)
- `keycloak` = `http://localhost:8080`

## Swagger UI (en tiempo de ejecución)
Cada servicio con SpringDoc expone, además, la documentación interactiva:
- Swagger UI: `http://<host:puerto>/swagger-ui.html`
- Especificación JSON: `http://<host:puerto>/v3/api-docs`
