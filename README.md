```bash
backend/
└── src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── ...
│   │           └── fleettelemetry/
│   │
│   │               ├── domain/
│   │               │   ├── model/
│   │               │   ├── exception/
│   │               │   ├── service/
│   │               │   └── port/
│   │               │
│   │               ├── application/
│   │               │   ├── port/
│   │               │   │   ├── input/
│   │               │   │   └── output/
│   │               │   └── service/
│   │               │
│   │               └── infrastructure/
│   │                   ├── adapter/
│   │                   │   ├── input/
│   │                   │   │   └── rest/
│   │                   │   └── output/
│   │                   │       ├── persistence/
│   │                   │       ├── cache/
│   │                   │       └── messaging/
│   │                   │
│   │                   ├── configuration/
│   │                   └── exception/
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-local.yml
│       └── application-server.yml
│
└── test/
```