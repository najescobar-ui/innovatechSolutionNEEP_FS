# Innovatech Solutions

Plataforma de gestión de proyectos basada en arquitectura de microservicios con autenticación centralizada, service discovery, circuit breakers y observabilidad end-to-end.

## Contexto del Negocio

**Innovatech Solutions** es una empresa de desarrollo de software a medida y
consultoría tecnológica con más de 120 empleados. Sus equipos son
multidisciplinarios (backend, frontend, DevOps, UX, gestores de proyecto) y
están distribuidos en distintas ubicaciones geográficas.

**Problema que resuelve la plataforma:**
- No hay visibilidad en tiempo real del estado de los proyectos.
- La asignación de recursos humanos se hace de forma manual y poco eficiente.
- Los directivos no cuentan con indicadores centralizados para tomar
  decisiones con datos concretos.

## Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| Frontend | React 18.3 |
| Autenticación | Keycloak 24.x (OAuth2 / OpenID Connect) |
| API Gateway | Spring Cloud Gateway |
| BFF | Spring Boot |
| Microservicios | Spring Boot + Spring Data JPA + Hibernate 6.4 |
| Service Discovery | Netflix Eureka |
| Circuit Breaker | Resilience4j 2.2 |
| Base de datos | PostgreSQL |
| Pool de conexiones | HikariCP |
| Mensajería SMTP (dev) | MailHog |
| Observabilidad | Prometheus 2.51 + Grafana 10.4 |
| Contenedores | Docker + Docker Compose |

## Componentes y Puertos

| Servicio | Host interno | Puerto | URL local |
|----------|--------------|--------|-----------|
| Frontend | - | 3000 | http://localhost:3000 |
| Keycloak | keycloak | 8080 | http://localhost:8080 |
| API Gateway | api-gateway | 9000 | http://localhost:9000 |
| BFF | bff | 8084 | - |
| ms-proyectos | ms-proyectos | 8081 | - |
| ms-recursos | ms-recursos | 8082 | - |
| ms-analitica | ms-analitica | 8083 | - |
| Eureka Server | eureka-server | 8761 | http://localhost:8761 |
| DB Proyectos | db-proyectos | 5432 | - |
| DB Recursos | db-recursos | 5433 | - |
| DB Analítica | db-analitica | 5434 | - |
| MailHog (SMTP) | mailhog | 1025 | - |
| MailHog (UI) | mailhog | 8025 | http://localhost:8025 |
| Prometheus | prometheus | 9090 | http://localhost:9090 |
| Grafana | grafana | 3001 | http://localhost:3001 |

Toda la comunicación interna ocurre sobre la red Docker `innovatech-net`.

## Arquitectura General

```mermaid
graph TB
    User([Usuario])

    subgraph "Cliente"
        FE[Frontend<br/>React 18.3<br/>:3000]
    end

    subgraph "Identidad"
        KC[Keycloak 24.x<br/>realm: innovatech<br/>:8080]
    end

    subgraph "Edge"
        GW[API Gateway<br/>:9000<br/>Rate limit 10 req/s]
    end

    subgraph "Orquestación"
        BFF[BFF<br/>:8084<br/>Resilience4j + Factory Method]
    end

    subgraph "Service Discovery"
        EUREKA[Eureka Server<br/>:8761]
    end

    subgraph "Microservicios"
        MS1[ms-proyectos<br/>:8081]
        MS2[ms-recursos<br/>:8082]
        MS3[ms-analitica<br/>:8083]
    end

    subgraph "Persistencia"
        DB1[(db-proyectos<br/>PostgreSQL :5432)]
        DB2[(db-recursos<br/>PostgreSQL :5433)]
        DB3[(db-analitica<br/>PostgreSQL :5434)]
    end

    subgraph "Notificaciones"
        MH[MailHog<br/>SMTP :1025]
    end

    subgraph "Observabilidad"
        PROM[Prometheus 2.51]
        GRAF[Grafana 10.4<br/>:3001]
    end

    User --> FE
    FE -->|1. Login| KC
    FE -->|2. Bearer JWT| GW
    GW -->|3. Valida JWK| KC
    GW -->|4. Enruta| BFF
    BFF -->|5a| MS1
    BFF -->|5b| MS2
    BFF -->|5c| MS3
    MS1 --> DB1
    MS2 --> DB2
    MS3 --> DB3
    MS2 -.SMTP.-> MH
    MS3 -.REST.-> MS1

    BFF -.register/discover.-> EUREKA
    MS1 -.register.-> EUREKA
    MS2 -.register.-> EUREKA
    MS3 -.register.-> EUREKA
    GW -.discover.-> EUREKA

    PROM -.scrape /actuator/prometheus.-> GW
    PROM -.scrape.-> BFF
    PROM -.scrape.-> MS1
    PROM -.scrape.-> MS2
    PROM -.scrape.-> MS3
    PROM -.scrape.-> EUREKA
    GRAF --> PROM

    style FE fill:#61DAFB,color:#000
    style KC fill:#4D4D4D,color:#fff
    style GW fill:#6DB33F,color:#fff
    style BFF fill:#6DB33F,color:#fff
    style MS1 fill:#6DB33F,color:#fff
    style MS2 fill:#6DB33F,color:#fff
    style MS3 fill:#6DB33F,color:#fff
    style DB1 fill:#336791,color:#fff
    style DB2 fill:#336791,color:#fff
    style DB3 fill:#336791,color:#fff
    style PROM fill:#E6522C,color:#fff
    style GRAF fill:#F46800,color:#fff
```

> Diagramas C4 detallados (System Context, Container y sub-diagramas por
> patrón) disponibles en [`docs/diagramas/`](docs/diagramas/) (formato
> drawio XML) y [`docs/imagenes/`](docs/imagenes/) (PNG renderizado).

## Dominio de cada Microservicio

| Microservicio | Responsabilidad funcional |
|---|---|
| **ms-proyectos** | Core operativo. Ciclo de vida completo de proyectos: creación, tareas, asignación de responsables, estados y avance. Es el servicio con mayor crecimiento proyectado a medida que se sumen clientes. |
| **ms-recursos** | Gestión de disponibilidad (capacity) del recurso humano, asignaciones a proyectos y visibilidad entre equipos. Dispara notificaciones SMTP cuando hay cambios de asignación. |
| **ms-analitica** | Dashboards con KPIs para perfil **directivo**. Consume datos de los otros dos servicios vía REST interna; nunca duplica información. |

## Flujo de Autenticación

El Frontend obtiene un JWT directamente desde Keycloak antes de cualquier llamada al backend.

```mermaid
sequenceDiagram
    autonumber
    participant U as Usuario
    participant FE as Frontend (React)
    participant KC as Keycloak

    U->>FE: Ingresa credenciales
    FE->>KC: POST /realms/innovatech/protocol/openid-connect/token
    KC-->>FE: { access_token, refresh_token, expires_in }
    Note over FE: JWT en memoria<br/>(NO en localStorage)
    FE-->>U: Login OK, redirige a dashboard
```

## Flujo End-to-End de un Request

Caso: usuario PM solicita su dashboard. El BFF orquesta 3 llamadas paralelas con Circuit Breaker.

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant GW as API Gateway
    participant KC as Keycloak
    participant BFF
    participant E as Eureka
    participant P as ms-proyectos
    participant R as ms-recursos
    participant A as ms-analitica
    participant DBP as db-proyectos
    participant DBR as db-recursos
    participant DBA as db-analitica

    FE->>GW: GET /api/dashboard<br/>Authorization: Bearer JWT
    GW->>KC: GET /protocol/openid-connect/certs
    KC-->>GW: JWK Set (claves públicas)
    Note over GW: Verifica firma, expiración, roles<br/>Aplica rate limit (10 req/s)
    GW->>E: Discover bff
    E-->>GW: bff:8084
    GW->>BFF: GET /dashboard<br/>X-User-Role: PM

    par Llamadas paralelas con Circuit Breaker
        BFF->>E: Discover ms-proyectos
        E-->>BFF: ms-proyectos:8081
        BFF->>P: GET /proyectos?responsable=usuario123
        P->>DBP: SELECT * FROM proyectos WHERE responsable_id = ?
        DBP-->>P: ResultSet
        P-->>BFF: JSON proyectos
    and
        BFF->>R: GET /asignaciones?usuario=usuario123
        R->>DBR: SELECT * FROM asignaciones WHERE usuario_id = ?
        DBR-->>R: ResultSet
        R-->>BFF: JSON asignaciones
    and
        BFF->>A: GET /kpis/equipo
        A->>DBA: SELECT ... FROM kpis
        DBA-->>A: ResultSet
        A-->>BFF: JSON KPIs
    end

    Note over BFF: Factory Method según rol PM<br/>→ construye PMDashboardDto
    BFF-->>GW: PMDashboardDto
    GW-->>FE: JSON dashboard
    Note over FE: Renderiza dashboard
```

## Service Discovery (Eureka)

Todos los componentes server-side se registran en Eureka al arrancar, y consultan Eureka antes de invocar a otro servicio. Esto permite escalado horizontal transparente.

```mermaid
graph LR
    subgraph "Registro al arrancar"
        S1[ms-proyectos] -->|POST /eureka/apps| E[(Eureka<br/>:8761)]
        S2[ms-recursos] -->|POST /eureka/apps| E
        S3[ms-analitica] -->|POST /eureka/apps| E
        S4[bff] -->|POST /eureka/apps| E
        S5[api-gateway] -->|POST /eureka/apps| E
    end

    subgraph "Descubrimiento en runtime"
        CLIENT[Cliente<br/>BFF o Gateway] -->|GET /eureka/apps/MS-X| E
        E -->|host:puerto + instancias| CLIENT
    end

    style E fill:#1B9E77,color:#fff
```

## Manejo de Fallos: Circuit Breaker (Resilience4j)

Cada llamada del BFF a un microservicio está envuelta en un Circuit Breaker independiente. Si `ms-analitica` no responde en 3s, el breaker se abre y el BFF devuelve un fallback sin afectar a los otros dos servicios.

```mermaid
stateDiagram-v2
    [*] --> Closed: arranque
    Closed --> Open: umbral de fallos<br/>superado (>50% en 10 req)
    Open --> HalfOpen: timeout 30s
    HalfOpen --> Closed: request de prueba OK
    HalfOpen --> Open: request de prueba falla
    Open --> [*]: fallback al cliente

    note right of Closed
        Tráfico normal
        Mide latencia y errores
    end note

    note right of Open
        Cortocircuita llamadas
        BFF retorna fallback
        (cache o "no disponible")
    end note

    note right of HalfOpen
        Permite N requests
        de prueba
    end note
```

## Aislamiento de Datos

Cada microservicio accede **únicamente** a su propia base de datos. Si `ms-analitica` necesita datos de proyectos, lo hace vía REST al `ms-proyectos`, nunca conectándose directo a `db-proyectos`.

```mermaid
graph TB
    subgraph "Permitido"
        A1[ms-proyectos] --> D1[(db-proyectos)]
        A2[ms-recursos] --> D2[(db-recursos)]
        A3[ms-analitica] --> D3[(db-analitica)]
        A3 -.REST GET /proyectos/metricas.-> A1
    end

    subgraph "Prohibido"
        B1[ms-analitica] -.X.-> D4[(db-proyectos)]
    end

    style B1 fill:#E74C3C,color:#fff
    style D4 fill:#E74C3C,color:#fff
```

## Notificaciones por Correo

`ms-recursos` envía notificaciones SMTP cuando ocurre un cambio de asignación. En desarrollo todo va a MailHog.

```mermaid
sequenceDiagram
    autonumber
    participant MS as ms-recursos
    participant MH as MailHog (:1025)
    participant DEV as Desarrollador

    Note over MS: Detecta cambio de asignación
    MS->>MH: SMTP<br/>From: notificaciones@innovatech.cl<br/>To: usuario@empresa.cl
    MH-->>MS: 250 OK
    DEV->>MH: GET http://localhost:8025
    MH-->>DEV: Inbox UI con correos capturados
```

> En producción, el host SMTP se reemplaza por Gmail u otro proveedor mediante variables de entorno.

## Observabilidad

Prometheus hace scrape cada 15 segundos a los endpoints `/actuator/prometheus` de todos los servicios. Grafana lee de Prometheus y expone dashboards en `http://localhost:3001`.

```mermaid
graph LR
    subgraph "Métricas expuestas"
        S1[api-gateway<br/>/actuator/prometheus]
        S2[bff<br/>/actuator/prometheus]
        S3[ms-proyectos<br/>/actuator/prometheus]
        S4[ms-recursos<br/>/actuator/prometheus]
        S5[ms-analitica<br/>/actuator/prometheus]
        S6[eureka-server<br/>/actuator/prometheus]
    end

    PROM[(Prometheus<br/>scrape 15s)]
    GRAF[Grafana<br/>:3001]

    S1 --> PROM
    S2 --> PROM
    S3 --> PROM
    S4 --> PROM
    S5 --> PROM
    S6 --> PROM
    PROM --> GRAF

    style PROM fill:#E6522C,color:#fff
    style GRAF fill:#F46800,color:#fff
```

Métricas relevantes recolectadas:

- `jvm_memory_used_bytes` — RAM por servicio
- `process_cpu_usage` — CPU
- `http_server_requests_seconds` — latencia y conteo HTTP
- `resilience4j_circuitbreaker_state` — estado de cada breaker (closed/open/half-open)
- `resilience4j_circuitbreaker_calls` — calls exitosos y fallidos
- `hikaricp_connections_active` — conexiones activas al pool de BD

## Cómo Levantar el Entorno Local

```bash
# 1. Clonar el repo
git clone <repo-url>
cd innovatech-solutions

# 2. Levantar toda la infraestructura
docker compose up -d

# 3. Verificar registros en Eureka
open http://localhost:8761

# 4. Acceder al frontend
open http://localhost:3000
```

Credenciales de prueba (realm `innovatech` en Keycloak):

| Usuario | Rol | Password |
|---------|-----|----------|
| pm.test | PM | (ver `.env.example`) |
| dev.test | DEV | (ver `.env.example`) |

## URLs de Referencia

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:3000 |
| Keycloak Admin | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:9000 |
| MailHog UI | http://localhost:8025 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |

## Patrones de Diseño Aplicados

- **API Gateway**: punto único de entrada, autenticación, rate limiting.
- **BFF (Backend for Frontend)**: agregación y adaptación de respuestas según rol del usuario.
- **Factory Method**: en el BFF, construye el DTO de dashboard apropiado según rol (`PMDashboardDto`, `DevDashboardDto`, etc).
- **Circuit Breaker**: aislamiento de fallos entre microservicios.
- **Service Discovery**: descubrimiento dinámico vía Eureka.
- **Repository Pattern**: acceso a datos vía Spring Data JPA.
- **Database per Service**: aislamiento total de datos por microservicio.

## Consideraciones Éticas y de Cumplimiento

El diseño sigue los principios de **Ethically Aligned Design (IEEE)**, dado
que Innovatech maneja datos personales de más de 120 personas.

| Principio | Implementación en la plataforma |
|---|---|
| **Protección de datos y privacidad** | HTTPS/TLS en toda comunicación. Control de acceso por roles vía Keycloak. Minimización: cada microservicio solo accede a los datos que necesita. `ms-analitica` trabaja con métricas agregadas, no con datos individuales. |
| **Transparencia y trazabilidad** | Logging centralizado con trazabilidad distribuida. Los KPIs expuestos en Grafana son auditables. |
| **Resiliencia ética** | Cuando un servicio cae, el Circuit Breaker hace que el BFF retorne `"datos no disponibles"` antes que devolver datos erróneos que afecten decisiones sobre personas. Las métricas del CB en Prometheus permiten saber exactamente cuándo un servicio estuvo degradado. |
| **Accesibilidad** | Frontend siguiendo **WCAG 2.1**, responsivo para equipos distribuidos. |

---

_Documentación generada para el proyecto Innovatech Solutions._
