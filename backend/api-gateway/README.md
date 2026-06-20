# api-gateway

API Gateway (Spring Cloud Gateway reactivo) de Innovatech Solutions. Es la única
puerta de entrada pública: rutea el tráfico del frontend hacia el BFF y valida el
JWT emitido por Keycloak.

## Tabla técnica
| Aspecto | Detalle |
|---------|---------|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4, Spring Cloud Gateway (WebFlux) |
| Librerías | Spring Cloud Gateway Server (reactivo), Spring Security OAuth2 Resource Server, Eureka Client, Actuator, Micrometer/Prometheus |
| Patrones de diseño | **API Gateway**, ruteo con `StripPrefix`, JWT Resource Server (validación centralizada) |

## Ruteo
| Predicado | Destino | Filtro |
|-----------|---------|--------|
| `Path=/api/**` | `lb://bff` | `StripPrefix=1` |

Ejemplos: `GET /api/dashboard` → `bff:/dashboard`; `POST /api/auth/register` → `bff:/auth/register`.

## Seguridad
- Todo `/**` exige JWT válido, **excepto**: `OPTIONS` (preflight CORS), `/actuator/**` y `/api/auth/**` (registro público).
- CORS configurable con `CORS_ALLOWED_ORIGINS` (default `http://localhost:3000`).

## Variables de entorno
| Variable | Default | Descripción |
|----------|---------|-------------|
| `KEYCLOAK_JWK_URI` | `http://keycloak:8080/realms/innovatech/protocol/openid-connect/certs` | JWKS para validar el token |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Orígenes permitidos |
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` | Registro Eureka |

## Ejecución
```bash
docker compose up -d --build api-gateway
curl -i http://localhost:9000/api/dashboard   # 401 sin token; 200 con Bearer válido
```
Puerto: `9000`.
