# BFF — Backend For Frontend

Componente Spring Boot que orquesta las llamadas a los microservicios, aplica circuit breakers por endpoint y construye DTOs adaptados al rol del usuario. **No tiene base de datos propia**: solo agrega y adapta.

## Stack

- Java **25** LTS
- Spring Boot **4.0.x** (web)
- Spring Cloud **2025.1.0 "Oakwood"** (Eureka client, LoadBalancer, Circuit Breaker)
- Resilience4j 2.2 (circuit breakers)
- Spring Security (resource server JWT)
- Eureka client + LoadBalancer (descubre `ms-proyectos`, `ms-recursos`, `ms-analitica` por service-id)
- Actuator + Micrometer Prometheus (métricas)

## Estructura

```
bff/
├── src/main/java/cl/duoc/innovatech/bff/
│   ├── BffApplication.java
│   ├── config/                      # SecurityConfig, BffClientsConfig (RestClient builders)
│   ├── domain/                      # DTOs: DashboardDto (sealed), Proyecto/Recurso Summary y Response,
│   │                                #       Crear*/Actualizar*Request, UserRole
│   ├── service/                     # *Client (RestClient con @LoadBalanced),
│   │                                # *Service (envuelve calls con CircuitBreakerFactory),
│   │                                # DashboardDtoFactory (Factory Method por rol)
│   └── web/                         # Controllers REST que expone el BFF
├── src/main/resources/
│   └── application.yml
├── Dockerfile                       # build multi-stage Maven → JRE
└── pom.xml                          # hereda de innovatech-parent
```

## Endpoints expuestos (consumidos por el frontend vía API Gateway)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/dashboard` | DTO de dashboard adaptado al rol del JWT (PM / DEV / DIR) |
| GET | `/proyectos` | Lista de proyectos (passthrough a `ms-proyectos`) |
| POST | `/proyectos` | Crear proyecto |
| PATCH | `/proyectos/{id}` | Actualizar estado y/o responsable |
| DELETE | `/proyectos/{id}` | Eliminar proyecto |
| GET | `/recursos` | Lista de recursos |
| POST | `/recursos` | Crear recurso |
| PATCH | `/recursos/{id}` | Actualizar flag activo |
| DELETE | `/recursos/{id}` | Eliminar recurso |
| GET | `/kpis` | KPIs snapshot actual (de `ms-analitica`) |
| GET | `/kpis/historico?desde&hasta&puntos` | Serie temporal + deltas (rango ≤ 30 días) |

> El API Gateway aplica `StripPrefix=1`, por eso desde el browser el prefijo público es `/api/dashboard`, `/api/proyectos`, etc.

## Patrones aplicados

- **Backend For Frontend**: agrega múltiples microservicios en una sola respuesta para el frontend.
- **Factory Method** (`DashboardDtoFactory`): construye `PMDashboardDto` / `DevDashboardDto` / `DirDashboardDto` según el rol del usuario autenticado. La `sealed interface DashboardDto` obliga a cubrir todos los casos en el switch.
- **Circuit Breaker** (Resilience4j): cada llamada saliente está envuelta con `breakers.create("<endpoint>").run(...)` y un fallback explícito (`"datos no disponibles"`).
- **Service Discovery**: cada `*Client` usa `@LoadBalanced RestClient.Builder` y URLs `http://ms-proyectos`, `http://ms-recursos`, `http://ms-analitica` (service-ids de Eureka, no hostnames Docker).

## Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` | URL del Eureka Server |

(Las URLs de los microservicios se descubren vía Eureka, no necesitan config.)

## Instalación y ejecución

### Levantar como parte del stack (recomendado)

Desde la raíz del repo:

```bash
docker compose up -d eureka-server bff
```

(con sus dependencias: Keycloak, los `ms-*` y sus BDs).

Imagen: `innovatech/bff:dev`, puerto interno 8084 (no se publica al host — solo el Gateway en 9000 es público).

### Ejecutar localmente fuera de Docker

Necesitas `eureka-server` y los `ms-*` arriba. Después:

```bash
# desde la raiz del repo (Maven multi-modulo)
docker compose run --rm bff bash    # o ejecuta mvn dentro
mvn -pl bff -am spring-boot:run
```

### Verificar

```bash
curl http://localhost:9000/api/dashboard -H "Authorization: Bearer <jwt>"
# Para obtener el JWT, autenticate primero contra Keycloak:
#   http://localhost:8080/realms/innovatech/protocol/openid-connect/token
```

Actuator:
```bash
curl http://localhost:8084/actuator/health
curl http://localhost:8084/actuator/circuitbreakers   # estado de los breakers
curl http://localhost:8084/actuator/prometheus        # metricas Prometheus
```

## Probar los circuit breakers

Bajar uno de los downstream (ej. `ms-analitica`) y pegarle a `/api/kpis`:

```bash
docker compose stop ms-analitica
curl http://localhost:9000/api/kpis -H "Authorization: Bearer <jwt>"
# → {"status":"datos no disponibles"}  (fallback)
docker compose start ms-analitica
```

Tras 30s (`OPEN → HALF_OPEN`) y un request de prueba exitoso, el breaker vuelve a `CLOSED`.
