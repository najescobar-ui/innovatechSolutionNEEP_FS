# bff — Backend For Frontend

Orquesta las llamadas a los microservicios, aplica circuit breakers por endpoint,
construye dashboards según el rol del usuario y expone el **registro de cuentas**
(vía Keycloak Admin API). **No tiene BD propia**: agrega y adapta.

## Tabla técnica
| Aspecto | Detalle |
|---------|---------|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4, Spring Cloud 2025.1.0 |
| Librerías | Spring Web, Spring Security (OAuth2 Resource Server), Resilience4j (Circuit Breaker), Eureka Client + LoadBalancer, RestClient, Actuator, Micrometer/Prometheus, springdoc-openapi (Swagger) |
| Patrones de diseño | **Backend For Frontend**, Agregador/Passthrough, **Factory Method** (`DashboardDtoFactory`), Circuit Breaker, tipos *sealed* (`DashboardDto`), Global Exception Handler |

## Endpoints
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/register` | Registro público (crea cuenta en Keycloak con perfil DEV/PM/DIR + RUT) |
| GET | `/dashboard` | Dashboard según rol (lee el email del JWT para el DEV) |
| GET | `/projects` · `/resources` · `/tasks` | Passthrough con fallback |
| POST/PATCH/DELETE | `/projects` · `/tasks` | Passthrough de escritura |
| GET | `/kpis` · `/kpis/history` | KPIs desde ms-analytics |

## Variables de entorno
| Variable | Default |
|----------|---------|
| `KEYCLOAK_JWK_URI` | `http://keycloak:8080/realms/innovatech/protocol/openid-connect/certs` |
| `KEYCLOAK_ADMIN_URL` | `http://keycloak:8080` |
| `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` | `admin` / `admin` (para el Admin API) |
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` |

## Ejecución
```bash
docker compose up -d --build bff
# registro (público, vía gateway):
curl -X POST http://localhost:9000/api/auth/register -H 'Content-Type: application/json' \
  -d '{"firstName":"Ana","lastName":"Diaz","email":"ana@innovatech.cl","rut":"12.345.678-9","password":"secret","role":"DEV"}'
open http://localhost:8084/swagger-ui.html
```
Puerto: `8084`. Rutas públicas: `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/**`.
