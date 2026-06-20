# ms-projects

Microservicio del ciclo de vida de **proyectos** y **tareas**: creación, consulta,
asignación de tareas a recursos, estados y eliminación. Persiste en su propia BD
PostgreSQL (Database per Service).

## Tabla técnica
| Aspecto | Detalle |
|---------|---------|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4, Spring Cloud 2025.1.0 |
| Librerías | Spring Web, Spring Data JPA (Hibernate 7), Flyway, PostgreSQL, Eureka Client, Actuator, Micrometer/Prometheus, springdoc-openapi (Swagger), Lombok |
| Patrones de diseño | Arquitectura en capas (web/service/repository/entity), Repository, DTO, Global Exception Handler |

## Endpoints
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/projects` | Lista proyectos |
| GET | `/projects/{id}` | Proyecto por id (404 si no existe) |
| POST | `/projects` | Crea proyecto (201 + `Location`) |
| PATCH | `/projects/{id}` | Actualiza estado/owner |
| DELETE | `/projects/{id}` | Elimina (204/404) |
| GET | `/tasks` | Lista tareas; filtros `?projectId=&assigneeResourceId=&status=` |
| GET | `/tasks/{id}` | Tarea por id |
| POST | `/tasks` | Crea tarea |
| PATCH | `/tasks/{id}` | Actualiza tarea (estado, asignado, horas, fecha) |
| DELETE | `/tasks/{id}` | Elimina tarea |

`TaskStatus`: `TODO`, `IN_PROGRESS`, `DONE`, `BLOCKED`.

## Variables de entorno
| Variable | Default |
|----------|---------|
| `DB_PROJECTS_HOST` / `DB_PROJECTS_USER` / `DB_PROJECTS_PASSWORD` | `db-projects` / `projects_user` / `projects_dev` |
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` |

## Ejecución
```bash
docker compose up -d --build ms-projects
curl http://localhost:8081/projects
open http://localhost:8081/swagger-ui.html   # documentación OpenAPI
```
Puerto: `8081`. BD vía Flyway (`V001..V005`).
