# eureka-server

Servidor de descubrimiento de servicios (Netflix Eureka) de Innovatech Solutions.
Los microservicios (bff, ms-projects, ms-resources, ms-analytics, api-gateway) se
registran aquí y se descubren entre sí por nombre lógico (`lb://...`, `http://ms-projects`).

## Tabla técnica
| Aspecto | Detalle |
|---------|---------|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4, Spring Cloud Netflix |
| Librerías | Spring Cloud Netflix Eureka Server, Actuator, Micrometer/Prometheus |
| Patrones de diseño | **Service Discovery** (servidor de registro), Client-Side Discovery |

## Configuración
- No se registra a sí mismo (`register-with-eureka: false`, `fetch-registry: false`).
- `enable-self-preservation: false` en dev para evictar rápido instancias caídas.

## Variables de entorno
| Variable | Default | Descripción |
|----------|---------|-------------|
| `EUREKA_HOSTNAME` | `eureka-server` | Hostname de la instancia |
| `EUREKA_DEFAULT_ZONE` | `http://localhost:8761/eureka/` | Zona por defecto |

## Ejecución
```bash
docker compose up -d --build eureka-server
open http://localhost:8761   # dashboard de Eureka con los servicios registrados
```
Puerto: `8761`.
