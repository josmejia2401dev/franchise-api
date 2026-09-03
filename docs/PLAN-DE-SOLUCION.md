# Plan de Solución — API de Gestión de Franquicias

> **Cliente:** Nequi · **Tipo:** Reto Técnico · **Versión del reto:** 2.0 (Julio 2026)
> **Rol asumido:** Arquitecto de software senior — Java 17+, Arquitectura Hexagonal (Ports & Adapters), Clean Architecture, SOLID.
> **Estado:** Plan (sin código todavía). Aprobar antes de ejecutar.

---

## 1. Objetivo

Construir una **API REST reactiva de extremo a extremo** para gestionar franquicias, sucursales y productos, desplegada en **AWS** mediante **Terraform modular**, cumpliendo estrictamente los tres pilares de evaluación (WebFlux reactivo puro, arquitectura hexagonal scaffold Bancolombia, e IaC/AWS), además de persistencia en la nube, resiliencia completa (Resilience4j), testing reactivo con StepVerifier + JaCoCo, y documentación OpenAPI.

**Regla de oro del reto:** todo el código en inglés, sin puntos bloqueantes, y el candidato debe poder explicar línea por línea en la sustentación.

---

## 2. Análisis del dominio

### 2.1 Modelo de datos

```
Franchise (1) ───< (N) Branch (1) ───< (N) Product
```

| Entidad     | Atributos                          | Relación                          |
|-------------|------------------------------------|-----------------------------------|
| `Franchise` | `id`, `name`, `branches[]`         | Tiene N sucursales                |
| `Branch`    | `id`, `name`, `franchiseId`, `products[]` | Pertenece a 1 franquicia, tiene N productos |
| `Product`   | `id`, `name`, `stock`, `branchId`  | Pertenece a 1 sucursal            |

### 2.2 Decisión de agregado (DDD)

`Franchise` es el **aggregate root**. `Branch` y `Product` son entidades dentro del agregado. Esto simplifica invariantes de negocio (un producto pertenece a una sucursal que pertenece a una franquicia) y encaja bien con una persistencia orientada a documentos.

---

## 3. Endpoints (Criterios funcionales)

Todos implementados con **RouterFunctions + HandlerFunctions** (nunca `@RestController`).

| # | Operación                                   | Verbo   | Path propuesto                                                        |
|---|---------------------------------------------|---------|----------------------------------------------------------------------|
| 1 | Crear franquicia                            | `POST`  | `/api/v1/franchises`                                                  |
| 2 | Agregar sucursal a franquicia               | `POST`  | `/api/v1/franchises/{franchiseId}/branches`                          |
| 3 | Agregar producto a sucursal                 | `POST`  | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products`      |
| 4 | Eliminar producto de sucursal               | `DELETE`| `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}` |
| 5 | Modificar stock de producto                 | `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` |
| 6 | Producto con mayor stock por sucursal (en una franquicia) | `GET` | `/api/v1/franchises/{franchiseId}/products/top-stock` |
| 7 | Actualizar nombre de franquicia             | `PATCH` | `/api/v1/franchises/{franchiseId}/name`                              |
| 8 | Actualizar nombre de sucursal               | `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/name`         |
| 9 | Actualizar nombre de producto               | `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` |

**Endpoint 6 (detalle):** retorna, por cada sucursal de la franquicia, el producto de mayor stock — una lista `[{ branchId, branchName, product: {...} }]`. Se resuelve con operadores reactivos (`Flux.fromIterable` sobre sucursales + reducción con `reduce`/`collect`, nunca `java.util.stream`).

---

## 4. Pilar 1 — Programación Reactiva (Spring WebFlux)

### 4.1 Reglas duras (checklist anti-descalificación)

- [ ] **Solo `RouterFunction` + `Handler`.** Cero `@RestController`, `@GetMapping`, etc.
- [ ] **Cero `.block()`** en toda la aplicación (validar con búsqueda + ArchUnit).
- [ ] **Cero `try-catch`** dentro de flujos; usar `onErrorResume`, `onErrorReturn`, `onErrorMap`.
- [ ] **Cero `if` dentro de flujos**; usar `filter`, `switchIfEmpty`, `defaultIfEmpty`.
- [ ] **Cero `java.util.stream`** en contexto reactivo; usar `Flux`/`Mono`.
- [ ] `flatMap` cuando la operación devuelve `Publisher`; `map` para transformaciones síncronas.
- [ ] No mezclar MVC + WebFlux.

### 4.2 Patrones reactivos por caso

| Necesidad                                   | Operador(es)                                  |
|---------------------------------------------|-----------------------------------------------|
| Recurso no encontrado → 404                 | `switchIfEmpty(Mono.error(new NotFoundException(...)))` |
| Validación condicional                      | `filter(...).switchIfEmpty(Mono.error(...))`  |
| Encadenar llamadas al repositorio           | `flatMap`                                     |
| Transformar entidad → DTO                   | `map`                                         |
| Mayor stock por sucursal                    | `Flux ... reduce/collectList` + comparación reactiva |
| Manejo de errores centralizado              | Handler + `onErrorResume` que mapea a `ServerResponse` con status HTTP |

### 4.3 Manejo de errores

- Excepciones de dominio (`FranchiseNotFoundException`, `BranchNotFoundException`, `ProductNotFoundException`, `InvalidStockException`, `DuplicateNameException`).
- Un componente de mapeo error → `ServerResponse` (400/404/409/500) aplicado con `onErrorResume` en cada handler, o un `AbstractErrorWebExceptionHandler` reactivo global.

### 4.4 Backpressure (para sustentación)

Explicar que WebFlux/Reactor lo gestiona nativamente vía el protocolo `request(n)` de Reactive Streams: el suscriptor pide demanda y el productor no emite más allá de lo solicitado. Relevante en el endpoint 6 y en cualquier `Flux` desde el repositorio reactivo.

---

## 5. Pilar 2 — Arquitectura Hexagonal (Scaffold Bancolombia)

> **Scaffold oficial:** [`bancolombia/scaffold-clean-architecture`](https://github.com/bancolombia/scaffold-clean-architecture) — plugin Gradle que genera un proyecto multi-módulo con Clean Architecture (dependency rule hacia adentro). Se usará el **modo reactivo (Spring WebFlux)**, no imperativo.
>
> *Contenido de la documentación oficial reformulado para cumplir restricciones de licencia.*

### 5.1 Comandos de generación (plugin oficial)

El plugin expone la task `cleanArchitecture | ca` (con parámetros `package`, `type`, `name`, `coverage`) y tasks para generar módulos: `generateModel`, `generateUseCase`, `generateEntryPoint`, `generateDrivenAdapter`, `generateHelper`.

```bash
# 0. build.gradle con el plugin:
# plugins { id "co.com.bancolombia.cleanArchitecture" version "<latest>" }

# 1. Estructura base en modo REACTIVO
gradle cleanArchitecture --package=co.com.nequi.franchise --type=reactive --coverage=jacoco

# 2. Modelo de dominio (genera model + gateway)
gradle generateModel --name=Franchise
gradle generateModel --name=Branch
gradle generateModel --name=Product

# 3. Caso de uso
gradle generateUseCase --name=Franchise

# 4. Entry point reactivo (RouterFunctions + Handler, sin @RestController)
gradle generateEntryPoint --type=webflux

# 5. Driven adapter de persistencia reactiva (elegir uno)
gradle generateDrivenAdapter --type=mongodb          # MongoDB reactivo (recomendado)
# gradle generateDrivenAdapter --type=r2dbc          # PostgreSQL/MySQL reactivo

# 6. Resiliencia y secretos
gradle generateDrivenAdapter --type=secrets          # AWS Secrets Manager
# Resilience4j se agrega vía dependencias + config

# 7. Documentación OpenAPI
gradle generateEntryPoint --type=openapi             # si aplica el generador
```

> Los nombres exactos de `--type` de cada task se confirmarán contra la versión del plugin al ejecutar F0; la doc oficial evoluciona por versión.

### 5.2 Estructura de módulos que genera el scaffold

```
franchise-api/
├── applications/
│   └── app-service/                     ← Application (bootstrap): main + wiring + beans automáticos
├── domain/
│   ├── model/                           ← Modelos de dominio + PUERTOS (interfaces). Sin frameworks.
│   │   └── src/main/java/co/com/nequi/franchise/model/
│   │       └── franchise/
│   │           ├── Franchise.java       (class con @Builder Lombok)
│   │           ├── Branch.java
│   │           ├── Product.java
│   │           └── gateways/            ← Puertos de salida (out-ports)
│   │               └── FranchiseRepository.java
│   └── usecase/                         ← Casos de uso. Solo Reactor (Mono/Flux).
│       └── src/main/java/co/com/nequi/franchise/usecase/
│           └── franchise/FranchiseUseCase.java
└── infrastructure/
    ├── entry-points/
    │   └── reactive-web/                ← RouterFunctions + Handlers + DTOs (records)
    │       └── .../api/{RouterRest, Handler, dto}
    ├── driven-adapters/
    │   └── reactive-mongo/ (o r2dbc)    ← Implementa el puerto FranchiseRepository
    │       └── .../{RepositoryAdapter, data, mapper}
    └── helpers/                         ← Utilidades comunes (resilience config, etc.)
```

Capas según la doc oficial: **Domain** (`model` + `usecase`), **Infrastructure** (`entry-points`, `driven-adapters`, `helpers`) y **Application** (`app-service`, único módulo con `public static void main`).

### 5.3 Regla de dependencia

- Dependencias apuntan **hacia adentro**: `entry-points` → `usecase` → `model`; `driven-adapters` → `usecase`/`model`.
- El **dominio no importa Spring, Mongo, ni nada de infraestructura**.
- **Puertos** (interfaces) viven en `domain/model/.../gateways`; **adaptadores** en `infrastructure`.

### 5.4 Convenciones exigidas

- **Lombok `@Builder`** en entidades de dominio (idempotencia + menos verbosidad). Evaluar `@Value`/`@With` para inmutabilidad.
- **Java Records** para DTOs de request/response en entry-points.
- **`jakarta.*`** en lugar de `javax.*`.
- Todo en **inglés**.

---

## 6. Pilar 3 — AWS + Terraform (IaC)

### 6.1 Arquitectura AWS objetivo

```
Internet → ALB → ECS Service (Fargate, Auto Scaling) → Tarea Docker (imagen en ECR)
                                                          │
                                        Secrets Manager (credenciales BD)
                                                          │
                                        Persistencia en la nube (Mongo Atlas / RDS / DynamoDB)
```

- **ECR:** repositorio de imagen Docker.
- **ECS Fargate:** servicio con Auto Scaling (CPU/memoria o request count).
- **ALB:** expone el servicio (health check `/actuator/health`).
- **Secrets Manager:** credenciales de BD inyectadas como `secrets` de la task definition (nunca en texto plano).
- **IAM:** roles con **mínimo privilegio** (task role vs execution role separados).

### 6.2 Terraform modular

```
infra/terraform/
├── modules/
│   ├── network/        (VPC, subnets, SGs)
│   ├── ecr/
│   ├── ecs/            (cluster, task def, service, autoscaling)
│   ├── alb/
│   ├── iam/
│   └── secrets/
├── environments/
│   ├── dev/    (main.tf, backend.tf, terraform.tfvars)
│   ├── staging/
│   └── prod/
├── variables.tf
├── outputs.tf
└── backend.tf          (remote state en S3 + lock en DynamoDB)
```

- **Remote state** en S3 + bloqueo con DynamoDB.
- **Multi-entorno** vía módulos parametrizados (o workspaces).
- Principios IaC: reproducibilidad, idempotencia, versionamiento.

---

## 7. Persistencia

**Opción recomendada:** MongoDB Atlas con `spring-boot-starter-data-mongodb-reactive` (driver reactivo puro, encaja con el agregado `Franchise` y evita joins). 

**Alternativa:** PostgreSQL en RDS con **R2DBC** (reactivo). DynamoDB con el enhanced async client también es válido. 

Cualquiera que sea la elección, debe ser **reactiva** (nada de JPA/JDBC bloqueante) y **accesible desde AWS**, con credenciales vía Secrets Manager.

> Decisión a confirmar con el operador antes de implementar. El plan detallado asume **MongoDB Atlas reactivo**.

---

## 8. Patrones de resiliencia (Resilience4j — completos)

| Patrón          | Implementación                                                                 |
|-----------------|--------------------------------------------------------------------------------|
| Circuit Breaker | `resilience4j-reactor` con operador `.transform(CircuitBreakerOperator.of(cb))` sobre los `Mono`/`Flux` del adaptador de persistencia. |
| Timeout         | `TimeLimiter` reactivo o `.timeout(Duration)` de Reactor.                       |
| Retry           | `RetryOperator` con backoff exponencial configurado.                           |
| Backpressure    | Nativo en WebFlux (explicar `request(n)`, `onBackpressureBuffer/Drop` si aplica). |

Configuración por `application.yml` + beans reactivos. Aplicados en los driven-adapters (borde de I/O), no en el dominio.

---

## 9. Testing

- **StepVerifier** (`reactor-test`) para todos los flujos reactivos. **Prohibido `.block()` en tests.**
- **JUnit 5 + Mockito** para unit tests de use cases y handlers.
- **Tests de integración** para router + persistencia (embedded Mongo reactivo / Testcontainers).
- **JaCoCo** con umbral de cobertura; los tests validan **escenarios de negocio** (no getters/setters).
- **ArchUnit** (recomendado) para verificar la regla de dependencia y prohibir `@RestController`/`.block()`.

---

## 10. Documentación y empaquetado

- **OpenAPI/Swagger** (`springdoc-openapi-starter-webflux-ui`) con todos los endpoints documentados y ejemplos request/response.
- **README.md** que permita levantar el proyecto desde cero (local con Docker Compose + Mongo, y despliegue AWS).
- **Docker** multi-stage build (build con Gradle + runtime JRE slim), imagen no-root, publicada en ECR.

---

## 11. Entrega

- Repositorio público (GitHub/GitLab).
- Historial de commits limpio y descriptivo (progreso real por fases).
- App desplegada y accesible en la nube para la sustentación.

---

## 12. Hoja de ruta por fases

| Fase | Entregable | Foco |
|------|-----------|------|
| **F0 — Setup** | Scaffold Bancolombia generado con `gradle cleanArchitecture --type=reactive` (+ `generateModel/UseCase/EntryPoint/DrivenAdapter`), Git init, dependencias base | Base hexagonal |
| **F1 — Dominio** | Entidades (`Franchise`/`Branch`/`Product` con `@Builder`), puertos, excepciones de dominio | Pilar 2 |
| **F2 — Use Cases** | `FranchiseUseCase` con los 9 casos, 100% reactivo, sin `if`/`try-catch` | Pilares 1 y 2 |
| **F3 — Persistencia** | Driven adapter Mongo reactivo + mappers + config | Pilar 1 + persistencia |
| **F4 — Entry Points** | Router + Handlers + DTOs (records) + manejo de errores reactivo | Pilar 1 |
| **F5 — Resiliencia** | Resilience4j (CB + Timeout + Retry) en adapters | Resiliencia |
| **F6 — Docs + Docker** | OpenAPI, README, Dockerfile multi-stage | Documentación/empaquetado |
| **F7 — IaC/AWS** | Terraform modular (network, ecr, ecs, alb, iam, secrets), remote state, multi-entorno | Pilar 3 |
| **F8 — Tests** | StepVerifier + JUnit5/Mockito + integración + JaCoCo + ArchUnit | Testing |
| **F9 — Deploy** | Build imagen → ECR → ECS Fargate tras ALB, verificación end-to-end | Entrega |

---

## 13. Riesgos y puntos de descalificación a vigilar

- Cualquier `.block()`, `try-catch` en flujo, `if` en flujo, `java.util.stream`, o `@RestController` → **descalificación**. Mitigación: ArchUnit + revisión + búsqueda automatizada en CI.
- Credenciales en texto plano → usar Secrets Manager sí o sí.
- `main.tf` monolítico → Terraform modular obligatorio.
- No poder explicar el código → entender cada operador reactivo y decisión de diseño antes de la sustentación.

---

## 14. Decisiones pendientes de confirmar con el operador

1. **Motor de persistencia**: ¿MongoDB Atlas reactivo (recomendado) o RDS PostgreSQL con R2DBC?
2. **Multi-entorno Terraform**: ¿módulos por entorno (recomendado) o workspaces?
3. **Repositorio destino**: ¿GitHub o GitLab? ¿nombre del repo?
4. **Grupo/paquete base**: p. ej. `co.com.nequi.franchise`.

> Una vez aprobado este plan y resueltas las 4 decisiones, arranco por **F0 (setup del scaffold)**.
