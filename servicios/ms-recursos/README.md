# ms-recursos

Microservicio responsable de la gestión del talento: profesionales, sus roles funcionales (DEV / QA / DEVOPS / DESIGNER / PM), su capacidad semanal en horas y su estado de actividad. Persiste en su propia BD PostgreSQL.

## Stack

- Java **25** LTS
- Spring Boot **4.0.x** (web + data-jpa + actuator)
- Hibernate 7 + Spring Data JPA
- Flyway
- PostgreSQL 16 (BD `db-recursos`, puerto interno 5432, mapeo host 5433)
- Eureka client
- Micrometer Prometheus

## Endpoints

| Método | Ruta | Body | Respuesta |
|--------|------|------|-----------|
| GET | `/recursos` | — | `List<RecursoDto>` |
| GET | `/recursos/{id}` | — | `RecursoDto` o `IllegalArgumentException` |
| POST | `/recursos` | `CrearRecursoRequest` | 201 + `RecursoDto` |
| PATCH | `/recursos/{id}` | `ActualizarRecursoRequest` (`activo`) | `RecursoDto` o 404 |
| DELETE | `/recursos/{id}` | — | 204 o 404 |

### DTOs

```java
record CrearRecursoRequest(
    String nombre,
    String email,
    RolRecurso rol,
    Integer horasSemanales,
    String competencias
) {}

record ActualizarRecursoRequest(
    Boolean activo
) {}
```

`RolRecurso` (enum): `DEV` | `PM` | `QA` | `DESIGNER` | `DEVOPS`.

> **Nota**: el `RolRecurso` es el perfil funcional del recurso. No confundir con los roles de plataforma definidos en Keycloak (`PM` / `DEV` / `DIR`), que controlan permisos en el frontend/BFF.

## Reglas de negocio

- **Email único**: al crear, valida que el email no exista. Si existe, lanza `IllegalArgumentException`.
- **Activo por defecto**: todo recurso nuevo se crea con `activo = true`.
- **PATCH solo flag activo**: hoy el endpoint de actualización solo permite cambiar `activo` (true/false). El frontend solo lo expone como "marcar inactivo" para DIR.

## Estructura

```
servicios/ms-recursos/
├── src/main/java/cl/duoc/innovatech/recursos/
│   ├── MsRecursosApplication.java
│   ├── config/
│   ├── entity/                   # Recurso, RolRecurso (enum)
│   ├── repository/               # RecursoRepository (con existsByEmail)
│   ├── service/                  # RecursoService (@Transactional por metodo)
│   ├── dto/                      # RecursoDto, Crear/ActualizarRecursoRequest
│   └── web/                      # RecursoController
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
├── Dockerfile
└── pom.xml
```

## Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `EUREKA_DEFAULT_ZONE` | `http://eureka-server:8761/eureka/` | URL del Eureka Server |
| `DB_RECURSOS_HOST` | `db-recursos` | Hostname Docker de la BD |
| `DB_RECURSOS_NAME` | `recursos` | Nombre de la BD |
| `DB_RECURSOS_USER` | `recursos_user` | Usuario PostgreSQL |
| `DB_RECURSOS_PASSWORD` | `recursos_dev` | Password PostgreSQL |

## Ejecución y prueba

```bash
docker compose up -d db-recursos eureka-server ms-recursos
```

Puerto interno: 8082.

### Probar (vía Gateway con JWT)

```bash
TOKEN="<jwt obtenido de Keycloak>"

# Listar
curl http://localhost:9000/api/recursos -H "Authorization: Bearer $TOKEN"

# Crear
curl -X POST http://localhost:9000/api/recursos \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Perez","email":"ana@innovatech.cl","rol":"DEV","horasSemanales":40,"competencias":"Java, Spring"}'

# Marcar inactivo
curl -X PATCH http://localhost:9000/api/recursos/1 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"activo":false}'

# Borrar
curl -X DELETE http://localhost:9000/api/recursos/1 -H "Authorization: Bearer $TOKEN"
```

### Verificar persistencia

```bash
docker exec -it db-recursos psql -U recursos_user -d recursos -c "SELECT id, nombre, rol, activo FROM recursos;"
```

## Patrones aplicados

- **Repository Pattern** (Spring Data JPA).
- **Database per Service**.
- **Migraciones declarativas** con Flyway.
- **DTO + Mapper inline**: el service mapea `Recurso → RecursoDto` con un método `toDto()` privado (sin MapStruct innecesario para este shape).
