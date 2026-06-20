# Arquetipos Maven

## Aclaración importante

Este proyecto **no usa arquetipos Maven generadores** (`mvn archetype:generate` con `archetype-resources/`). En su lugar, usa el patrón equivalente y más común en proyectos Spring Boot reales: un **POM padre multi-módulo** (`innovatech-parent`) que cumple la misma función que un arquetipo:

- Centraliza versiones del stack para todos los componentes backend.
- Define dependencias compartidas (Spring Boot starter parent, Spring Cloud BOM, Resilience4j BOM, MapStruct, springdoc).
- Configura plugins de build (compiler con annotation processors de Lombok + MapStruct, spring-boot-maven-plugin).
- Sirve como base obligatoria para crear un nuevo microservicio: el módulo nuevo solo declara `<parent>` y obtiene **todo el stack heredado**.

Si en algún momento se requiere un arquetipo generador real, se puede empaquetar el contenido de `ms-projects/` como `archetype-resources/` y publicarlo, pero hoy no es necesario.

## POM padre — `pom.xml`

Ubicación: raíz del repo. `groupId: cl.duoc.innovatech`, `artifactId: innovatech-parent`, `version: 0.1.0-SNAPSHOT`, `packaging: pom`.

Hereda directamente de `spring-boot-starter-parent:4.0.0`.

### Versiones gestionadas

| Propiedad | Valor |
|-----------|-------|
| `java.version` | 25 |
| `spring-cloud.version` | 2025.1.0 (Oakwood) |
| `resilience4j.version` | 2.2.0 |
| `mapstruct.version` | 1.6.3 |
| `lombok.version` | 1.18.44 *(mínimo requerido para Java 25)* |
| `lombok-mapstruct-binding.version` | 0.2.0 |
| `springdoc-openapi.version` | 2.6.0 |

### `dependencyManagement` (BOMs importados)

- `spring-cloud-dependencies` 2025.1.0 (Eureka, Gateway, Circuit Breaker, OpenFeign).
- `resilience4j-bom` 2.2.0.
- `mapstruct` 1.6.3.
- `springdoc-openapi-starter-webmvc-ui` 2.6.0.

### `pluginManagement`

- `maven-compiler-plugin`: configurado con `release=25` y annotation processors (Lombok, MapStruct, lombok-mapstruct-binding) ya enchufados. Cualquier módulo hijo los hereda **sin tener que repetir nada**.
- `spring-boot-maven-plugin`: con exclusión de Lombok del jar final.
- `maven-surefire-plugin` 3.5.2 y `maven-failsafe-plugin` 3.5.2.

### Módulos declarados

```xml
<modules>
  <module>eureka-server</module>
  <module>api-gateway</module>
  <module>bff</module>
  <module>ms-projects</module>
  <module>ms-resources</module>
  <module>ms-analytics</module>
</modules>
```

## Cómo usar el "arquetipo" para crear un nuevo componente

Pasos para agregar un microservicio nuevo (ej. `ms-notifications`):

### 1. Crear la estructura de carpetas

```
ms-notifications/
├── src/main/java/cl/duoc/innovatech/notificaciones/
│   └── MsNotificacionesApplication.java
├── src/main/resources/
│   └── application.yml
├── Dockerfile
└── pom.xml
```

### 2. Crear el `pom.xml` del módulo

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <!-- Hereda TODO del padre: versiones, plugins, annotation processors -->
    <parent>
        <groupId>cl.duoc.innovatech</groupId>
        <artifactId>innovatech-parent</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>ms-notifications</artifactId>
    <name>Innovatech Solutions :: MS Notificaciones</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- Si necesita persistencia, copiar el bloque de ms-projects -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**No declarar versiones**: el padre las gestiona.

### 3. Agregar el módulo al padre

Editar el `pom.xml` raíz:

```xml
<modules>
  ...
  <module>ms-notifications</module>
</modules>
```

### 4. Configuración base del Spring Boot

`MsNotificacionesApplication.java`:

```java
package cl.duoc.innovatech.notificaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsNotificacionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsNotificacionesApplication.class, args);
    }
}
```

`application.yml`:

```yaml
server:
  port: 8085

spring:
  application:
    name: ms-notifications

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_DEFAULT_ZONE:http://eureka-server:8761/eureka/}
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 5. Dockerfile (copiar de un microservicio existente)

Multi-stage `eclipse-temurin:25-jdk` para build → `eclipse-temurin:25-jre-alpine` para runtime. Plantilla idéntica a la de `ms-projects/Dockerfile`.

### 6. Service en `docker-compose.yml`

```yaml
ms-notifications:
  build:
    context: .
    dockerfile: ms-notifications/Dockerfile
  image: innovatech/ms-notifications:dev
  container_name: ms-notifications
  networks: [innovatech-net]
  ports: ["8085:8085"]
  environment:
    EUREKA_DEFAULT_ZONE: http://eureka-server:8761/eureka/
  depends_on:
    eureka-server:
      condition: service_started
  restart: unless-stopped
```

### 7. Build y verificar

```bash
docker compose build ms-notifications
docker compose up -d ms-notifications
# verifica que se registro:
curl http://localhost:8761/eureka/apps | grep ms-notifications
```

## Convenciones que hereda automáticamente cualquier módulo nuevo

- **Java 25** con `--release 25`.
- **Annotation processors** de Lombok + MapStruct ya enchufados.
- **Lombok 1.18.44** (versión mínima compatible con Java 25; pinneada para evitar `ExceptionInInitializerError`).
- **`-parameters`** activo en el compiler.
- **Spring Cloud 2025.1** disponible sin declarar versión.
- **`spring-boot-maven-plugin`** con Lombok excluido del jar final.
- **Imagen Docker propia por módulo** (decisión cerrada: no usamos buildpacks ni Jib).

## Gotchas conocidos del stack

- Spring Cloud Gateway 5.x cambió de namespace: starter `spring-cloud-starter-gateway-server-webflux` y YAML `spring.cloud.gateway.server.webflux.routes`. El namespace viejo se ignora **silenciosamente** sin errores.
- Lombok ≥ 1.18.44 es **mandatorio** para Java 25. Versiones anteriores fallan al compilar con `ExceptionInInitializerError: TypeTag :: UNKNOWN` aunque el módulo no use Lombok directamente (basta con que el padre lo declare como annotation processor).
- En módulos con `spring-cloud-starter-loadbalancer` + `spring-cloud-starter-netflix-eureka-client`, **NO** declarar un único `@Bean @LoadBalanced RestClient.Builder`: el cliente Eureka interno lo confunde y entra en boot loop. Patrón correcto: dos beans (uno `@Primary` sin LB, otro `@LoadBalanced` inyectado explícitamente). Ver `bff/.../config/BffClientsConfig.java`.

## Resumen visual

```
innovatech-parent (pom.xml raíz)
│
├─ Spring Boot 4.0 starter parent
├─ Spring Cloud BOM 2025.1
├─ Resilience4j BOM 2.2
├─ Annotation processors (Lombok + MapStruct)
└─ Plugins (compiler, spring-boot, surefire, failsafe)
    │
    └── Hereda en:
        ├─ eureka-server
        ├─ api-gateway
        ├─ bff
        ├─ ms-projects
        ├─ ms-resources
        └─ ms-analytics
```
