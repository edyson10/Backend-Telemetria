# Fleet Telemetry Backend — Docker, Swagger/OpenAPI y AWS

## 1. Objetivo

Dejar el backend ejecutable en tres escenarios:

1. Java local + PostgreSQL local + Redis local.
2. Docker Compose con backend + PostgreSQL + Redis.
3. AWS CloudFormation creando una EC2 que ejecuta los tres containers con Docker Compose.

## 2. Requisitos

- Java 21
- Maven Wrapper
- Docker Desktop
- Git
- AWS CLI (opcional si se usa la consola)
- Cuenta AWS
- EC2 Key Pair

## 3. Swagger/OpenAPI

Agregar al `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.1.1</version>
</dependency>
```

Copiar `docs/openapi/OpenApiConfiguration.java` a:

```text
src/main/java/com/movilidad/backendtelemetria/infrastructure/configuration/OpenApiConfiguration.java
```

Endpoints:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
http://localhost:8080/v3/api-docs.yaml
```

La documentación dinámica de springdoc debe considerarse la fuente principal. `docs/openapi/openapi.yaml` es una especificación estática de referencia.

## 4. Local: PostgreSQL y Redis en la máquina

Verifique:

```text
PostgreSQL: localhost:5432
Redis:      localhost:6379
Database:   telemetry
```

Use el perfil local:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

Pruebas:

```text
GET http://localhost:8080/actuator/health
GET http://localhost:8080/api/v1/vehicles
GET http://localhost:8080/api/v1/alerts?limit=20
GET http://localhost:8080/api/v1/routes?originLat=6.2442&originLng=-75.5812&destinationLat=6.2500&destinationLng=-75.5900
```

POST:

```text
POST http://localhost:8080/api/v1/telemetry
```

Body: `docs/test-data/telemetry-local.json`

## 5. Docker Compose completo

Desde la raíz del backend:

```powershell
docker compose up -d --build
docker compose ps
```

Logs:

```powershell
docker compose logs -f backend
```

URLs:

```text
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

Detener:

```powershell
docker compose down
```

Eliminar también los datos de PostgreSQL:

```powershell
docker compose down -v
```

## 7. Circuit Breaker

Consultar:

```text
GET http://localhost:8080/actuator/circuitbreakers
```

La instancia es:

```text
route-service
```

Configuración:

```text
window = 5 llamadas
minimum calls = 5
failure threshold = 50%
OPEN wait = 10s
HALF_OPEN permitted calls = 2
```

## 8. Estructura

```text
backendtelemetria/
├── .mvn/
├── src/
├── Dockerfile
├── .dockerignore
├── docker-compose.yml
├── .env-local
├── .env-docker
├── pom.xml
├── docs/
│   ├── openapi/
│   │   ├── openapi.yaml
│   │   └── OpenApiConfiguration.java
│   └── test-data/
└── infrastructure/
    └── cloudformation/
        └── back-telemetria.yml
```

SRC detallado

```text
backend/
└── src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── ...
│   │           └── backendtelemetria/
│   │               ├── domain/
│   │               │   ├── model/
│   │               │   └── exception/   
│   │               ├── application/
│   │               │   ├── port/
│   │               │   │   ├── input/
│   │               │   │   └── output/
│   │               │   ├── service/
│   │               │   └── exception
│   │               └── infrastructure/
│   │                   ├── adapter/
│   │                   │   ├── input/
│   │                   │   │   └── rest/
│   │                   │   │       ├── controller/
│   │                   │   │       ├── mapper/
│   │                   │   │       ├── request/
│   │                   │   │       └── response/
│   │                   │   └── output/
│   │                   │       ├── persistence/
│   │                   │       │   ├── controller/
│   │                   │       │   ├── mapper/
│   │                   │       │   ├── request/
│   │                   │       │   └── response/
│   │                   │       ├── redis/
│   │                   │       └── route/
│   │                   ├── configuration/
│   │                   └── exception/
│   └── resources/
│       └── db/
│           └── migration/
│       ├── application.yml
│       ├── application-local.yml
│       └── application-server.yml
│
└── test/
```