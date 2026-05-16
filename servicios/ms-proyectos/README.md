# ms-proyectos

Microservicio responsable del ciclo de vida de los proyectos: creación, consulta, actualización parcial y eliminación. Persiste en su propia base de datos PostgreSQL (Database per Service).

## Stack

- Java **25** LTS
- Spring Boot **4.0.x** (web + data-jpa + actuator)
- Hibernate 7 + Spring Data JPA
- Flyway (migraciones versionadas)
- PostgreSQL 16 (BD `db-proyectos`, puerto interno 5432)
- Eureka client
- Micrometer Prometheus
- Lombok (uso moderado)

## Endpoints

| Método | Ruta | Body | Respuesta |
|--------|------|------|-----------|
| GET | `/proyectos` | — | `List<ProyectoDto>` |
| GET | `/proyectos/{id}` | — | `ProyectoDto` o 404 |
| POST | `/proyectos` | `CrearProyectoRequest` | 201 + `ProyectoDto` + header `Location` |
| PATCH | `/proyectos/{id}` | `ActualizarProyectoRequest` (estado y/o responsableId) | `ProyectoDto` o 404 |
| DELETE | `/proyectos/{id}` | — | 204 o 404 |

### DTOs

```java
record CrearProyectoRequest(
    String nombre,
    String descripcion,
    EstadoProyecto estado,
    LocalDate fechaInicio,
    LocalDate fechaFinPlanificada,
    String responsableId
) {}

record ActualizarProyectoRequest(
    EstadoProyecto estado,
    String responsableId
) {}
```

`EstadoProyecto`: `PLANIFICACION` | `EN_CURSO` | `COMPLETADO` | `CANCELADO`.

## Estructura

```
servicios/ms-proyectos/
├── src/main/java/cl/duoc/innovatech/proyectos/
│   ├── MsProyectosApplication.java
│   ├── config/
│   ├── entity/                   # Proyecto, EstadoProyecto (enum)
│   ├── repository/               # ProyectoRepository (Spring Data JPA)
│   ├── service/                  # ProyectoService (@Transactional)
│   ├── dto/                      # ProyectoDto, CrearProyectoRequest, ActualizarProyectoRequest
│   └── web/                      # ProyectoController
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/             # V001__schema_proyectos.sql, V002__seed_proyectos.sql
├── Dockerfile
└── pom.xml
```

## Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` | URL del Eureka Server |
| `DB_PROYECTOS_HOST` | `db-proyectos` | Hostname Docker de la BD |
| `DB_PROYECTOS_NAME` | `proyectos` | Nombre de la BD |
| `DB_PROYECTOS_USER` | `proyectos_user` | Usuario PostgreSQL |
| `DB_PROYECTOS_PASSWORD` | `proyectos_dev` | Password PostgreSQL |

## Ejecución y prueba

### Como parte del stack

```bash
docker compose up -d db-proyectos eureka-server ms-proyectos
```

Imagen: `innovatech/ms-proyectos:dev`, puerto interno 8081.

### Probar (a través del Gateway, requiere JWT)

```bash
TOKEN="<jwt obtenido de Keycloak>"

# Listar
curl http://localhost:9000/api/proyectos -H "Authorization: Bearer $TOKEN"

# Crear
curl -X POST http://localhost:9000/api/proyectos \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"nombre":"Demo","descripcion":"Test","estado":"PLANIFICACION","fechaInicio":"2026-06-01","fechaFinPlanificada":"2026-07-01","responsableId":"user1"}'

# Patch (solo estado o solo responsable, ambos opcionales)
curl -X PATCH http://localhost:9000/api/proyectos/1 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"estado":"EN_CURSO"}'

# Borrar
curl -X DELETE http://localhost:9000/api/proyectos/1 -H "Authorization: Bearer $TOKEN"
```

### Probar directo al microservicio (sin gateway, solo en red Docker)

```bash
docker exec -it ms-proyectos curl http://ms-proyectos:8081/proyectos
```

### Verificar persistencia

```bash
docker exec -it db-proyectos psql -U proyectos_user -d proyectos -c "SELECT id, nombre, estado FROM proyectos;"
```

### Verificar migraciones Flyway

```bash
curl http://localhost:8081/actuator/flyway   # estado de cada migracion
```

## Patrones aplicados

- **Repository Pattern** (Spring Data JPA).
- **Database per Service**: solo este microservicio accede a `db-proyectos`. Otros consumen vía REST.
- **Migraciones declarativas** con Flyway, versionadas en `db/migration/`.
- **Validación transaccional** con `@Transactional` a nivel de clase, métodos de lectura con `readOnly = true`.
