# ms-analitica

Microservicio agregador de KPIs cross-organización. Consume `ms-proyectos` y `ms-recursos` vía REST (Eureka) y persiste **snapshots históricos** en su propia BD para series temporales (sparklines, deltas, evolución por rango de fechas).

> **Decisión 2026-05-16**: este microservicio antes era stateless. Se decidió darle BD propia (`db-analitica`) para soportar la vista "Evolución del periodo" del Dashboard. Sigue cumpliendo Database per Service: la BD solo aloja agregados, no fuentes primarias.

## Stack

- Java **25** LTS
- Spring Boot **4.0.x** (web + data-jpa + actuator + scheduling)
- Hibernate 7 + Spring Data JPA
- Flyway
- PostgreSQL 16 (BD `db-analitica`, puerto host 5434)
- Eureka client + LoadBalancer (para llamar a `ms-proyectos` / `ms-recursos`)
- Resilience4j (circuit breaker)
- Micrometer Prometheus

## Endpoints

| Método | Ruta | Query params | Respuesta |
|--------|------|--------------|-----------|
| GET | `/analitica/kpis` | — | `KpiResponse` (snapshot actual calculado on-the-fly) |
| GET | `/analitica/kpis/historico` | `puntos` (default 12), `desde` (ISO date), `hasta` (ISO date) | `HistoricoResponse` + deltas vs ~30 días atrás. **Rango máximo 30 días** (400 si excede). |

### Respuesta de `/analitica/kpis`

```json
{
  "status": "ok",
  "proyectosActivos": 12,
  "proyectosAtrasados": 1,
  "totalRecursosActivos": 8,
  "capacidadSemanalTotalHoras": 320,
  "promedioHorasPorRecurso": 40.0,
  "porcentajeUtilizacion": 0.78,
  "recursosPorRol": { "DEV": 4, "QA": 2, "PM": 1, "DEVOPS": 1 },
  "proyectosPorEstado": { "PLANIFICACION": 3, "EN_CURSO": 9 }
}
```

Si los servicios fuente (`ms-proyectos`/`ms-recursos`) están caídos:

```json
{ "status": "datos no disponibles", ... ceros ... }
```

### Respuesta de `/analitica/kpis/historico`

```json
{
  "status": "ok",
  "utilizacion":      [ { "t": "2026-05-01T00:00:00Z", "v": 0.74 }, ... ],
  "proyectosActivos": [ { "t": "2026-05-01T00:00:00Z", "v": 10  }, ... ],
  "deltas": {
    "porcentajeUtilizacion": 0.04,
    "proyectosActivos": 2,
    "recursosActivos": 1
  }
}
```

## Cómo se generan los snapshots

1. **Scheduler** (`KpiSnapshotService.capturar()`) configurable con `SNAPSHOTS_CRON` (default cada 5 minutos en dev). Llama a `KpiService.calcular()` y persiste un nuevo `KpiSnapshot`.
2. **Backfill inicial**: al primer arranque (tabla vacía), genera 12 puntos semanales sintéticos con jitter ±15% sobre el valor actual para que el sparkline tenga algo desde el día 1. Cuando el scheduler corre repetidamente, los puntos reales reemplazan al backfill.

## Entidad `KpiSnapshot`

| Columna | Tipo | Nota |
|---------|------|------|
| `id` | BIGSERIAL PK | |
| `captured_at` | TIMESTAMPTZ | Indexada |
| `proyectos_activos` | INT | |
| `proyectos_atrasados` | INT | |
| `total_recursos_activos` | INT | |
| `capacidad_semanal_horas` | INT | |
| `promedio_horas_por_recurso` | DOUBLE PRECISION | |
| `porcentaje_utilizacion` | DOUBLE PRECISION | 0.0 a 1.0 |

Migración: `db/migration/V1__kpi_snapshots.sql`.

## Estructura

```
servicios/ms-analitica/
├── src/main/java/cl/duoc/innovatech/analitica/
│   ├── MsAnaliticaApplication.java   # @EnableScheduling
│   ├── config/
│   ├── dto/                          # KpiResponse, HistoricoResponse, ProyectoView, RecursoView
│   ├── entity/                       # KpiSnapshot
│   ├── repository/                   # KpiSnapshotRepository (consultas por rango)
│   ├── service/                      # KpiService, KpiSnapshotService, ProyectosClient, RecursosClient
│   └── web/                          # AnaliticaController
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/V1__kpi_snapshots.sql
├── Dockerfile
└── pom.xml
```

## Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` | URL del Eureka Server |
| `DB_ANALITICA_HOST` | `db-analitica` | Hostname Docker de la BD |
| `DB_ANALITICA_NAME` | `analitica` | Nombre de la BD |
| `DB_ANALITICA_USER` | `analitica_user` | Usuario PostgreSQL |
| `DB_ANALITICA_PASSWORD` | `analitica_dev` | Password PostgreSQL |
| `SNAPSHOTS_ENABLED` | `true` | Activa el scheduler |
| `SNAPSHOTS_CRON` | `0 */5 * * * *` | Cadencia del scheduler (Spring cron) |
| `SNAPSHOTS_BACKFILL_WEEKS` | `12` | Cantidad de puntos sintéticos iniciales |

## Ejecución y prueba

```bash
docker compose up -d db-analitica eureka-server ms-proyectos ms-recursos ms-analitica
```

Puerto interno: 8083.

### Verificar snapshot actual

```bash
TOKEN="<jwt>"
curl http://localhost:9000/api/kpis -H "Authorization: Bearer $TOKEN"
```

### Verificar histórico con rango

```bash
curl "http://localhost:9000/api/kpis/historico?desde=2026-04-20&hasta=2026-05-16" \
  -H "Authorization: Bearer $TOKEN"
```

Si el rango supera 30 días devuelve `400 Bad Request`.

### Forzar un snapshot ahora (vía Actuator scheduling)

Los `@Scheduled` no tienen endpoint de trigger manual. Para acelerar pruebas, reducir `SNAPSHOTS_CRON` (ej. `0 * * * * *` cada minuto) y recrear el contenedor.

### Inspeccionar la BD

```bash
docker exec -it db-analitica psql -U analitica_user -d analitica \
  -c "SELECT captured_at, proyectos_activos, porcentaje_utilizacion FROM kpi_snapshots ORDER BY captured_at DESC LIMIT 10;"
```

## Patrones aplicados

- **Repository Pattern** + **Database per Service**.
- **Snapshot / Periodic Aggregation**: tabla `kpi_snapshots` poblada por `@Scheduled`, base para series temporales sin acoplar al esquema de los servicios fuente.
- **Circuit Breaker** en los `*Client` para no fallar duro si `ms-proyectos`/`ms-recursos` caen mientras se calcula un KPI.
- **Service Discovery** vía Eureka (URLs lógicas `http://ms-proyectos`).

## Cálculo de utilización (proxy)

Mientras no exista una tabla de asignaciones recurso↔proyecto, la utilización se calcula como:

```
utilizacion = min(1.0, promedioHorasPorRecurso / 40h)
```

Cuando exista la entidad de asignaciones, reemplazar por `horasAsignadas / capacidadTotal`. La interfaz pública (`porcentajeUtilizacion`) no cambia.
