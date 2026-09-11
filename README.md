# Sistema de Monitoreo y Telemetría de Flotas

Solución desarrollada para la prueba técnica **Senior Fullstack
Developer -- Sistema de Monitoreo y Telemetría de Flotas**.

El proyecto implementa un sistema capaz de recibir coordenadas del GPS 
de múltiples vehículos, detectar vehículos detenidos, evitar duplicados,
almacenar histórico, mantener información reciente en caché, exponer
alertas y mostrar el estado de la flota desde un dashboard web y una
aplicación móvil.

> **Estado:** prototipo funcional desplegado en AWS + Vercel, con
> aplicación móvil Ionic/Angular.

------------------------------------------------------------------------

## 1. Resumen de la solución

La solución está dividida en tres clientes/componentes principales:

-   **Backend:** Java 21 + Spring Boot.
-   **Frontend web:** React + Vite.
-   **Aplicación móvil:** Ionic + Angular + Capacitor.
-   **Persistencia:** PostgreSQL.
-   **Caché:** Redis.
-   **Contenedores:** Docker + Docker Compose.
-   **Infraestructura:** AWS EC2 provisionada mediante CloudFormation.
-   **Frontend web desplegado:** Vercel.

La idea principal es separar el histórico de la información de consulta
rápida:

-   PostgreSQL conserva el histórico de telemetría y las alertas.
-   Redis conserva información temporal como última posición,
    deduplicación y seguimiento de parada.
-   El backend concentra las reglas de negocio.
-   Los clientes web y móvil consumen la misma API.

------------------------------------------------------------------------

## 2. Arquitectura

``` mermaid
flowchart LR
    SIM[Simulador de Telemetría] --> API[Spring Boot API]

    WEB[Frontend React - Vercel] --> API
    MOB[App Ionic/Angular] --> API

    API --> ING[Servicio de Ingesta]
    API --> VEH[Consulta de Vehículos]
    API --> ALT[Servicio de Alertas]
    API --> ROUTE[Servicio de Rutas]

    ING --> REDIS[(Redis)]
    ING --> PG[(PostgreSQL)]
    ALT --> PG
    VEH --> REDIS
    VEH --> PG

    ROUTE --> CB[Circuit Breaker]
```

### Flujo de una telemetría

``` text
Vehículo / Simulador
        |
        v
POST /api/v1/telemetry
        |
        v
Validación
        |
        v
Detección de duplicado
        |
        +---- duplicado ----> respuesta controlada
        |
        v
Detección de estado
        |
        +---- misma posición > 1 min
        |              |
        |              v
        |         STOPPED + alerta
        |
        v
PostgreSQL
        |
        v
Redis: última posición / estado
```

------------------------------------------------------------------------

# 3. Backend

## Stack

-   Java 21
-   Spring Boot
-   Spring Web MVC
-   Spring Data JPA
-   Hibernate
-   PostgreSQL
-   Flyway
-   Redis
-   Resilience4j
-   OpenAPI / Swagger
-   Actuator
-   Maven
-   JUnit / Mockito
-   Cuenta AWS
-   EC2 Key Pair

## Arquitectura interna

Se utiliza una arquitectura modular inspirada en **Hexagonal / Clean
Architecture**.

``` text
src/main/java/com/movilidad/backendtelemetria/

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

### ¿Por qué esta separación?

La regla de negocio no debería depender directamente de PostgreSQL,
Redis o HTTP.

Por ejemplo, el caso de uso de ingesta conoce una interfaz como
`TelemetryRepositoryPort`, pero no necesita saber cómo funciona JPA.

Esto permite cambiar PostgreSQL por otra solución o Redis por otro
proveedor sin reescribir las reglas principales.

------------------------------------------------------------------------

# 4. Endpoints principales

## Health

``` http
GET /actuator/health
```

Ejemplo:

``` json
{
  "status": "UP"
}
```

## Ingesta

``` http
POST /api/v1/telemetry
Content-Type: application/json
```

Ejemplo de payload utilizado por el backend:

``` json
{
  "vehicleId": "VH-001",
  "latitude": 6.2442,
  "longitude": -75.5812,
  "speed": 35.5,
  "timestamp": "2026-09-10T19:00:00Z"
}
```

> Si el contrato DTO del repositorio utiliza nombres diferentes, el
> simulador debe utilizar exactamente los nombres definidos por
> `TelemetryRequest`.

## Vehículos

``` http
GET /api/v1/vehicles
```

Devuelve los vehículos identificados a partir de la telemetría y su
último estado conocido.

## Alertas

``` http
GET /api/v1/alerts?limit=20
```

Devuelve las alertas recientes.

## Rutas

``` http
GET /api/v1/routes
```

Endpoint utilizado para demostrar el módulo de ruteo y el Circuit
Breaker.

------------------------------------------------------------------------

# 5. Anti-duplicados

La solución utiliza Redis para evitar procesar dos veces la misma
telemetría durante una ventana corta.

Se genera una huella a partir de los datos relevantes de la telemetría y
se almacena temporalmente.

TTL utilizado:

``` text
10 segundos
```

Ejemplo:

``` text
Mensaje A
vehicleId = VH-001
lat       = 6.2442
lng       = -75.5812
timestamp = 19:00:00

        ↓

Redis registra fingerprint

        ↓

Mismo mensaje nuevamente dentro de 10 s

        ↓

DUPLICADO
```

Un mensaje con la misma posición pero un timestamp diferente no se
considera automáticamente duplicado.

Esto es importante porque una telemetría legítima puede reportar la
misma posición varias veces mientras un vehículo está detenido.

------------------------------------------------------------------------

# 6. Detección de vehículo detenido

La regla funcional es:

> Si un vehículo mantiene la misma coordenada durante más de un minuto,
> se genera una alerta de vehículo detenido.

El seguimiento del inicio de la parada se mantiene en Redis.

``` text
19:00:00  posición A
19:00:05  posición A
19:00:10  posición A
...
19:01:00  posición A
19:01:01  posición A
             |
             v
          STOPPED
```

Cuando el vehículo cambia de posición:

``` text
posición A
   ↓
posición B
   ↓
MOVING
   ↓
se elimina el seguimiento de parada
```

### Decisión importante

El sistema necesita recibir otra telemetría para confirmar que la
posición continúa igual. No se crea una alerta por un temporizador
aislado sin recibir datos del vehículo.

------------------------------------------------------------------------

# 7. Alertas

Cuando el estado pasa a `STOPPED`, se crea un `TelemetryAlert`.

Esto permite separar:

-   telemetría histórica;
-   estado actual;
-   eventos relevantes para operación.

El frontend web y la aplicación móvil consumen las alertas desde:

``` http
GET /api/v1/alerts?limit=20
```

------------------------------------------------------------------------

# 8. PostgreSQL vs Redis

La separación responde a dos necesidades diferentes.

### PostgreSQL

Se utiliza para:

-   histórico;
-   telemetría;
-   alertas;
-   información que debe permanecer disponible.

### Redis

Se utiliza para:

-   última posición;
-   estado reciente;
-   deduplicación;
-   inicio de una parada;
-   información temporal.

La razón es que consultar constantemente el histórico en PostgreSQL para
saber la última posición de todos los vehículos sería innecesariamente
costoso.

Redis permite resolver este tipo de consulta de manera mucho más rápida.

------------------------------------------------------------------------

# 9. Flyway

Las tablas se crean mediante migraciones de Flyway.

La aplicación no depende de que Hibernate cree las tablas
automáticamente.

Se utiliza:

``` yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Esto significa que Hibernate valida que el esquema esperado coincida con
la base de datos, mientras Flyway controla los cambios del esquema.

------------------------------------------------------------------------

# 10. Circuit Breaker

El servicio de rutas está protegido con Resilience4j.

Configuración principal:

``` http
GET /actuator/circuitbreakers
```
``` http
GET /actuator/circuitbreakerevents
```


``` text
Sliding window: 5 llamadas
Minimum calls: 5
Failure threshold: 50%
Open state: 10 segundos
Half-open calls: 2
```

Estados:

``` text
CLOSED
  |
  | demasiados errores
  v
OPEN
  |
  | después de 10 segundos
  v
HALF_OPEN
  |
  +---- funciona ----> CLOSED
  |
  +---- falla --------> OPEN
```

La intención es evitar que una dependencia externa caída provoque que
toda la aplicación continúe intentando llamarla indefinidamente.

### Producción

En una arquitectura de producción, para una falla de persistencia además
incorporaría un mecanismo durable de cola/outbox para desacoplar la
recepción de la telemetría de la persistencia. En este prototipo se
priorizó demostrar la resiliencia del servicio de ruteo mediante Circuit
Breaker y mantener la solución sencilla de ejecutar.

------------------------------------------------------------------------

# 11. Consistencia al eliminar un vehículo

La prueba solicita explicar cómo garantizar que al eliminar un vehículo
se limpie tanto la persistencia como la caché.

En producción propondría un proceso de consistencia eventual:

``` text
DELETE vehículo
      |
      v
Transacción PostgreSQL
      |
      +--> elimina/inhabilita histórico según política
      |
      v
Outbox Event
      |
      v
Evento VehicleDeleted
      |
      v
Consumidor
      |
      v
Elimina claves Redis
```

¿Por qué no haría simplemente dos operaciones independientes?

Porque podría ocurrir:

``` text
DELETE PostgreSQL -> OK
DELETE Redis      -> FALLA
```

y quedarían datos antiguos en Redis.

Con un evento/outbox se puede reintentar la limpieza sin perder la
intención de la operación.

Para el prototipo no se implementó un CRUD completo de vehículos porque
la prueba está enfocada en telemetría y monitoreo; esta decisión queda
documentada como estrategia de producción.

------------------------------------------------------------------------

# 12. Frontend Web

Tecnologías:

-   React
-   Vite
-   JavaScript
-   Lucide React

El frontend es una SPA.

Funcionalidades:

-   dashboard;
-   listado de vehículos;
-   estado MOVING / STOPPED;
-   alertas;
-   mapa visual;
-   actualización automática;
-   integración con API real.

## Actualización

Se utiliza polling cada 5 segundos.

``` text
Dashboard
   |
   +-- carga vehículos
   |
   +-- carga alertas
   |
   +-- cada 5 segundos
          |
          +--> GET /vehicles
          |
          +--> GET /alerts
```

Se eligió polling porque para el alcance de la prueba permite una
actualización suficientemente rápida sin introducir la complejidad de
WebSockets.

En una plataforma con miles de vehículos y muchos usuarios evaluaría
SSE/WebSockets o una arquitectura basada en eventos.

------------------------------------------------------------------------

# 13. Frontend en Vercel

El frontend está desplegado en Vercel.

Como Vercel funciona bajo HTTPS y el backend del prototipo está expuesto
mediante HTTP, se configuró un rewrite/proxy:

``` text
Browser
   |
   | HTTPS
   v
Vercel /api/...
   |
   | proxy
   v
AWS EC2 :8080
```

Comando para ejecutar el frontend en local.

``` powershell
npm run dev
```

Esto evita que el navegador intente realizar directamente una llamada
HTTP desde una página HTTPS.

Las variables de entorno de Vite se configuran directamente en Vercel y
no se almacenan en Git.

------------------------------------------------------------------------

# 14. Aplicación móvil

Tecnologías:

-   Ionic
-   Angular
-   Capacitor

La aplicación consume el mismo backend.

Incluye:

-   dashboard;
-   vehículos;
-   alertas;
-   rutas;
-   navegación;
-   visualización del estado;
-   actualización de información;
-   conexión con backend.

Comando para ejecutar la aplicacion en local.

``` powershell
npx ionic serve --no-open
```

El prototipo se diseñó pensando en un flujo orientado al conductor y en
una futura evolución hacia una aplicación con telemetría real.

------------------------------------------------------------------------

# 15. Arquitectura móvil Offline First

En producción no asumiría que el teléfono tendrá conexión permanente.

Ejemplo:

``` text
Vehículo
   |
   | GPS
   v
App móvil
   |
   +---- Internet disponible ----> API
   |
   +---- sin Internet
             |
             v
       almacenamiento local
             |
             v
       conexión recuperada
             |
             v
       sincronización
```

## Almacenamiento local

Usaría una cola local persistente, por ejemplo SQLite.

Cada evento tendría:

-   identificador local;
-   vehículo;
-   timestamp del GPS;
-   coordenadas;
-   velocidad;
-   estado de sincronización.

Los eventos quedarían:

``` text
PENDING
SYNCING
SYNCED
FAILED
```

## ¿Qué pasa con 10 minutos sin conexión?

No intentaría enviar los 10 minutos de información inmediatamente sin
control.

La aplicación:

1.  guarda localmente;
2.  conserva el orden temporal;
3.  recupera la conexión;
4.  agrupa los registros;
5.  sincroniza por lotes;
6.  utiliza reintentos con backoff;
7.  marca los registros confirmados.

También usaría un identificador único por evento para que una
retransmisión no cree duplicados.

## ¿Cómo evitar saturar el servidor?

-   batch de eventos;
-   límite de tamaño por lote;
-   backoff;
-   compresión cuando sea necesario;
-   prioridad a eventos importantes;
-   eliminación de puntos redundantes cuando la precisión requerida lo
    permita;
-   límite de concurrencia.

------------------------------------------------------------------------

# 16. Estrategia de batería

Leer GPS cada segundo durante todo el día no es una buena decisión.

Usaría estrategias combinadas:

### Distancia mínima

En lugar de reportar siempre:

``` text
cada 1 segundo
```

se puede reportar cuando el vehículo se haya desplazado una distancia
mínima.

### Intervalos adaptativos

Por ejemplo:

``` text
Vehículo en movimiento:
frecuencia mayor

Vehículo detenido:
frecuencia menor
```

### Background location

Usaría las APIs de ubicación en segundo plano proporcionadas por
Android/iOS y respetaría las políticas de cada sistema operativo.

### Activity recognition

Si el sistema detecta que el conductor está detenido, puede reducir
temporalmente la frecuencia del GPS.

### Balance precisión/batería

No siempre se necesita la máxima precisión.

Se puede utilizar:

``` text
alta precisión → cuando realmente aporta valor
precisión moderada → operación normal
baja frecuencia → vehículo detenido
```

La estrategia final dependería del nivel de precisión requerido por el
negocio.

------------------------------------------------------------------------

# 17. Simulación de telemetría

La prueba requiere:

-   mínimo 5 vehículos;
-   envío cada 2 a 5 segundos;
-   10% duplicados;
-   5% payloads inválidos.

Se incluye un simulador aislado en:

``` text
simulator/simulator.py
```

Ejemplo:

``` bash
python simulator.py --base-url http://localhost:8080
```

Para AWS:

``` bash
python simulator.py --base-url http://EC2_PUBLIC_DNS:8080
```

El simulador:

-   mantiene 5 vehículos;
-   mueve sus coordenadas;
-   conserva el último payload;
-   puede reenviar exactamente el mismo payload para probar
    deduplicación;
-   genera payloads inválidos;
-   muestra estadísticas en consola.

### Ejemplo de salida esperada

``` text
Telemetry simulator started
Vehicles: 5
Interval: 2-5 seconds
Duplicate rate: 10%
Invalid rate: 5%

VH-001 -> 202 ACCEPTED
VH-002 -> 202 ACCEPTED
VH-003 -> 400 INVALID
VH-004 -> 202 ACCEPTED
VH-005 -> DUPLICATE
```

> Las proporciones se comportan como porcentajes aproximados durante una
> ejecución; no se fuerza una distribución artificial en cada grupo de
> 20 mensajes.

------------------------------------------------------------------------

# 18. Pruebas manuales recomendadas

## 18.1 Health

``` bash
curl http://localhost:8080/actuator/health
```

Esperado:

``` json
{"status":"UP"}
```

## 18.2 Telemetría

``` bash
curl -X POST http://localhost:8080/api/v1/telemetry \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId":"VH-TEST-001",
    "latitude":6.2442,
    "longitude":-75.5812,
    "speed":30,
    "timestamp":"2026-09-10T19:00:00Z"
  }'
```

## 18.3 Duplicado

Enviar exactamente el mismo request dos veces dentro de 10 segundos.

Resultado esperado:

``` text
Primera petición -> aceptada
Segunda petición -> duplicada
```

## 18.4 Vehículo detenido

Enviar la misma posición con timestamps separados durante más de un
minuto.

Esperado:

``` text
MOVING
MOVING
MOVING
STOPPED
```

Después cambiar la posición:

``` text
MOVING
```

## 18.5 Alertas

``` bash
curl "http://localhost:8080/api/v1/alerts?limit=20"
```

## 18.6 Vehículos

``` bash
curl http://localhost:8080/api/v1/vehicles
```

------------------------------------------------------------------------

# 19. Levantar localmente

Requisitos:

-   Docker
-   Docker Compose

Desde la carpeta del backend:

``` bash
docker compose up -d --build
```

Verificar:

``` bash
docker compose ps
```

Levantar solo el redis:

``` bash
docker compose up -d redis
```

Esperado:

``` text
postgres   running/healthy
redis      running/healthy
backend    running
```

Health:

``` bash
curl http://localhost:8080/actuator/health
```

------------------------------------------------------------------------

# 20. Detener el entorno

``` bash
docker compose down
```

Para eliminar también el volumen de PostgreSQL:

``` bash
docker compose down -v
```

> Esto elimina los datos locales de PostgreSQL.

------------------------------------------------------------------------

# 21. Despliegue AWS

La infraestructura se provisiona con CloudFormation.

Componentes:

``` text
CloudFormation
      |
      v
EC2
      |
      v
Docker Compose
   ├── Spring Boot
   ├── PostgreSQL
   └── Redis
```

El archivo de CloudFormation crea:

-   EC2;
-   Security Group;
-   almacenamiento EBS;
-   instalación de Docker;
-   instalación de Docker Compose;
-   clonación del proyecto;
-   variables de servidor;
-   ejecución del stack Docker.

El backend utiliza un perfil `server` y el archivo `.env-server`.

------------------------------------------------------------------------

# 22. Variables de entorno

No se versionan credenciales.

Localmente:

``` text
.env-local
```

Servidor:

``` text
.env-server
```

Estos archivos contienen configuración sensible y se encuentran
excluidos mediante `.gitignore`.

En Vercel se configuran las variables directamente en el proyecto.

------------------------------------------------------------------------

# 23. Seguridad

Para la prueba se habilitó el acceso al puerto 8080 para poder demostrar
el servicio públicamente.

En producción:

-   restringiría SSH al rango administrativo;
-   colocaría el backend detrás de un Load Balancer;
-   utilizaría HTTPS;
-   usaría Secrets Manager o Parameter Store para secretos;
-   restringiría PostgreSQL y Redis a la red privada;
-   evitaría exponer directamente los puertos de infraestructura.

------------------------------------------------------------------------

# 24. Pruebas y calidad

Las pruebas unitarias se enfocan principalmente en reglas de negocio:

-   ingesta;
-   deduplicación;
-   detección de parada;
-   generación de alertas;
-   consulta de vehículos;
-   comportamiento del Circuit Breaker.

La intención no es perseguir cobertura por porcentaje, sino cubrir las
reglas que representan mayor riesgo funcional.

------------------------------------------------------------------------

# 25. Uso de IA

## Herramientas

Se utilizaron:

-   ChatGPT;
-   Claude Code;
-   herramientas de asistencia integradas al entorno de desarrollo.

## Usos

La IA se utilizó como apoyo para:

-   diseño inicial de arquitectura;
-   generación de boilerplate;
-   creación y refactorización de clases;
-   pruebas unitarias;
-   revisión de errores;
-   revisión de componentes del frontend y móvil cumpliendo con estándares de buenas prácticas y arquitecturas;
-   análisis de problemas de integración;
-   configuración Docker;
-   configuración AWS/CloudFormation;
-   documentación;
-   revisión de decisiones técnicas.

La IA no se utilizó como sustituto de las pruebas. Cada propuesta se
contrastó ejecutando el código y revisando el comportamiento real.

## Error / hallucination encontrado

Durante el desarrollo apareció una primera aproximación a la detección
de vehículos detenidos que comparaba únicamente dos mensajes y esperaba
que estuvieran separados por más de un minuto.

Al probarla con el simulador, se evidenció un problema: el simulador
envía telemetría cada pocos segundos, por lo que dos mensajes
consecutivos nunca tendrían un minuto de separación.

La solución fue cambiar el enfoque: mantener en Redis el momento en que
comenzó a repetirse la posición y evaluar cuánto tiempo ha permanecido
el vehículo en ese punto.

Este fue un buen ejemplo de por qué la salida de una herramienta de IA
debe validarse contra el comportamiento real del sistema y no aceptarse
solamente porque el código compile.

------------------------------------------------------------------------

# 26. Desafíos y soluciones

## AWS

Uno de los principales problemas durante el despliegue fue que la
instancia EC2 se creó correctamente, pero el backend inicialmente no
escuchaba en el puerto 8080.

La revisión del entorno permitió detectar que Docker no había quedado
instalado/configurado como se esperaba y posteriormente se corrigió el
proceso de instalación y despliegue.

También se corrigió la separación entre configuración local y servidor
mediante `.env-server`.

## HTTPS de Vercel vs HTTP de AWS

El frontend estaba servido por HTTPS y el backend inicialmente por HTTP.

El navegador bloqueaba la llamada directa.

La solución para el prototipo fue usar un rewrite/proxy de Vercel,
manteniendo la comunicación del navegador bajo HTTPS.

En producción se recomienda exponer también el backend mediante HTTPS.

## Cambio de estado detenido

La primera implementación no representaba correctamente el
comportamiento de un vehículo que reporta cada pocos segundos.

Se cambió a un seguimiento de inicio de parada en Redis.

------------------------------------------------------------------------

# 27. Qué haría diferente con más tiempo

Si el sistema fuera a evolucionar a producción, priorizaría:

1.  API HTTPS detrás de Application Load Balancer.
2.  PostgreSQL administrado, por ejemplo RDS.
3.  Redis administrado, por ejemplo ElastiCache.
4.  Cola de eventos para desacoplar la ingesta de la persistencia.
5.  Outbox para consistencia.
6.  WebSockets o SSE para actualización en tiempo real.
7.  Autenticación y autorización.
8.  Observabilidad con métricas, logs centralizados y trazas.
9.  Pruebas de carga.
10. Auto Scaling.
11. CI/CD.
12. Offline First completo en la aplicación móvil.
13. GPS adaptativo para optimizar batería.
14. Mapa geográfico real con Leaflet/Mapbox/Google Maps.
15. Gestión completa de vehículos y resolución de alertas.

------------------------------------------------------------------------

# 29. Conclusión

La solución busca demostrar no solamente que la aplicación funciona,
sino que las decisiones pueden evolucionar hacia una arquitectura de
producción.

Se priorizaron:

-   separación de responsabilidades;
-   persistencia adecuada para histórico;
-   caché para datos de alta frecuencia;
-   tolerancia a fallos;
-   pruebas de reglas críticas;
-   despliegue reproducible;
-   documentación;
-   y una propuesta clara de evolución hacia producción.
