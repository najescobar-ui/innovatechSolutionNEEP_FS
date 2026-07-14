# Innovatech · Arquetipo Maven de microservicio

Arquetipo Maven **personalizado** que genera un microservicio nuevo con la misma
estructura y stack que los servicios del proyecto (`ms-projects`, `ms-resources`):
Spring Boot 4 + JPA + Flyway + Eureka + Actuator + OpenAPI, heredando del POM padre
`innovatech-parent`.

Cumple el requisito del caso: *"componentes backend con arquetipos Maven personalizados"*.

## Qué genera

```
ms-<nombre>/
├── pom.xml                       # hereda de innovatech-parent
├── Dockerfile                    # multi-stage, usuario no-root
└── src/
    ├── main/
    │   ├── java/<paquete>/
    │   │   ├── ServiceApplication.java
    │   │   ├── config/           # GlobalExceptionHandler, OpenApiConfig
    │   │   ├── dto/              # SampleDto, Create/Update requests, ErrorResponse
    │   │   ├── entity/          # Sample (JPA)
    │   │   ├── exception/       # NotFoundException
    │   │   ├── repository/      # SampleRepository (Spring Data JPA)
    │   │   ├── service/         # SampleService (CRUD)
    │   │   └── web/             # SampleController (REST /samples)
    │   └── resources/
    │       ├── application.yml   # datasource, JPA, Flyway, Eureka, Actuator
    │       └── db/migration/     # V001__schema_sample.sql (Flyway)
    └── test/java/<paquete>/service/SampleServiceTest.java
```

## Uso

```bash
# 1) Instalar el POM padre en el repo local (una vez)
cd backend && mvn -N install

# 2) Instalar el arquetipo en el repo local (una vez)
cd backend/archetypes/innovatech-ms-archetype && mvn install

# 3) Generar un microservicio nuevo (en la carpeta donde quieras crearlo)
mvn archetype:generate -B \
  -DarchetypeGroupId=com.duoc \
  -DarchetypeArtifactId=innovatech-ms-archetype \
  -DarchetypeVersion=0.1.0-SNAPSHOT \
  -DgroupId=com.duoc \
  -DartifactId=ms-notifications \
  -Dpackage=com.duoc.notifications
```

Esto crea `ms-notifications/` compilable (`mvn -pl ms-notifications compile` desde el
reactor, o `mvn compile` dentro de la carpeta).

## Después de generar

El microservicio trae una entidad de ejemplo `Sample`. Para adaptarlo a tu dominio:

1. Renombrá `Sample` (entidad, DTOs, repository, service, controller) a tu concepto real.
2. Ajustá la migración Flyway `V001__schema_*.sql` al esquema real.
3. Cambiá `spring.application.name` en `application.yml` (es el nombre con que se registra en Eureka).
4. Agregá el módulo al `<modules>` del POM padre si querés que entre al reactor.
