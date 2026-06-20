# ms-analytics

Microservicio agregador de **KPIs**. Consume `ms-projects` y `ms-resources` vía REST
(Eureka) para calcular indicadores en tiempo real (utilización real = demanda de
tareas / capacidad, proyectos activos/atrasados, KPIs de tareas) y persiste
**snapshots históricos** para series temporales. BD propia (Database per Service).

## Tabla técnica
| Aspecto | Detalle |
|---------|---------|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4, Spring Cloud 2025.1.0 |
| Librerías | Spring Web, Spring Data JPA (Hibernate 7), Flyway, PostgreSQL, Eureka Client + LoadBalancer, Resilience4j (Circuit Breaker), Actuator, Micrometer/Prometheus, springdoc-openapi (Swagger) |
| Patrones de diseño | Arquitectura en capas, Repository, DTO, **Circuit Breaker** (Resilience4j), tareas programadas (snapshots), Global Exception Handler |

## Endpoints
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/analytics/kpis` | KPIs actuales (utilización real, tareas, recursos por rol, proyectos por estado) |
| GET | `/analytics/kpis/history` | Serie histórica; `?points=&from=&to=` (rango máx. 30 días) |

## Variables de entorno
| Variable | Default |
|----------|---------|
| `DB_ANALYTICS_HOST` / `DB_ANALYTICS_USER` / `DB_ANALYTICS_PASSWORD` | `db-analytics` / `analytics_user` / `analytics_dev` |
| `SNAPSHOTS_ENABLED` / `SNAPSHOTS_CRON` | `true` / `0 */5 * * * *` |
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` |

## Ejecución
```bash
docker compose up -d --build ms-analytics
curl http://localhost:8083/analytics/kpis
open http://localhost:8083/swagger-ui.html
```
Puerto: `8083` (host 5434 → BD). Los clients a projects/resources/tasks usan circuit breaker con fallback "datos no disponibles".
