# ms-resources

Microservicio de gestión del **talento**: profesionales, su rol funcional
(DEV/QA/DEVOPS/DESIGNER/PM), capacidad semanal y estado de actividad. Persiste en
su propia BD PostgreSQL (Database per Service).

## Tabla técnica
| Aspecto | Detalle |
|---------|---------|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4, Spring Cloud 2025.1.0 |
| Librerías | Spring Web, Spring Data JPA (Hibernate 7), Flyway, PostgreSQL, Eureka Client, Actuator, Micrometer/Prometheus, springdoc-openapi (Swagger), Lombok |
| Patrones de diseño | Arquitectura en capas, Repository, DTO, Global Exception Handler, excepción de dominio (`NotFoundException`→404) |

## Endpoints
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/resources` | Lista recursos |
| GET | `/resources/{id}` | Recurso por id (404 si no existe) |
| GET | `/resources/by-email?email=` | Resuelve recurso por email (lo usa el BFF) |
| POST | `/resources` | Crea recurso (201) |
| PATCH | `/resources/{id}` | Activa/desactiva |
| DELETE | `/resources/{id}` | Elimina (204/404) |

`ResourceRole`: `DEV`, `PM`, `QA`, `DESIGNER`, `DEVOPS`.

## Variables de entorno
| Variable | Default |
|----------|---------|
| `DB_RESOURCES_HOST` / `DB_RESOURCES_USER` / `DB_RESOURCES_PASSWORD` | `db-resources` / `resources_user` / `resources_dev` |
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` |

## Ejecución
```bash
docker compose up -d --build ms-resources
curl http://localhost:8082/resources
open http://localhost:8082/swagger-ui.html
```
Puerto: `8082` (host 5433 → BD). Flyway `V001..V003`.
