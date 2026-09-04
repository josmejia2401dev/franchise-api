# Franchise Management API

Reactive REST API to manage a network of commercial franchises, their branches
and products. Built with Spring WebFlux (fully non-blocking), Clean Architecture
(Bancolombia scaffold) and reactive MongoDB.

## Domain model

```
Franchise (1) ---< (N) Branch (1) ---< (N) Product
```

- **Franchise**: has a name and a list of branches.
- **Branch**: has a name, belongs to a franchise, and contains a list of products.
- **Product**: has a name and a stock, and belongs to a branch.

`Franchise` is the aggregate root; branches and products live inside it.

## Tech stack

- Java 21, Spring Boot 4, Spring WebFlux (RouterFunctions + Handlers)
- Reactive MongoDB (MongoDB Atlas)
- Resilience4j (Circuit Breaker + TimeLimiter + Retry)
- springdoc-openapi (Swagger UI)
- Gradle multi-module (Clean Architecture: domain / usecase / infrastructure / app)

## Requirements

- Java 21 (the Gradle toolchain resolves it automatically)
- A MongoDB Atlas cluster and its connection string
- The Gradle wrapper is included; no local Gradle install needed

## Configuration

Configuration is read from environment variables. For local runs, place a `.env`
file at the repository root (the values are loaded as environment variables).

| Variable | Description | Example |
|---|---|---|
| `SERVER_PORT` | HTTP port | `8080` |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins (comma-separated) | `http://localhost:4200` |
| `MONGODB_URI_TEMPLATE` | Mongo URI, with `{username}`/`{password}` placeholders | `mongodb+srv://{username}:{password}@cluster0.xxxx.mongodb.net/franchise?retryWrites=true&w=majority&appName=Cluster0` |
| `MONGODB_USERNAME` | Mongo user | `my-user` |
| `MONGODB_PASSWORD` | Mongo password (URL-encode special chars) | `my-pass` |

At start-up the app assembles the final Mongo URI by replacing the placeholders
in the template with the username/password, so no full connection string or
credentials are hardcoded.

Example `.env`:

```
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:8080
MONGODB_URI_TEMPLATE=mongodb+srv://{username}:{password}@cluster0.xxxx.mongodb.net/franchise?retryWrites=true&w=majority&appName=Cluster0
MONGODB_USERNAME=my-user
MONGODB_PASSWORD=my-pass
```

## Run locally

```bash
./gradlew :app-service:bootRun
```

The API starts on `http://localhost:8080`.

## API documentation (Swagger)

Once running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints

Base path: `/api/v1/franchises`

| # | Action | Method | Path |
|---|--------|--------|------|
| 1 | Create franchise | POST | `/api/v1/franchises` |
| 2 | Add branch | POST | `/api/v1/franchises/{franchiseId}/branches` |
| 3 | Add product | POST | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products` |
| 4 | Remove product | DELETE | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}` |
| 5 | Update product stock | PATCH | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` |
| 6 | Top-stock product per branch | GET | `/api/v1/franchises/{franchiseId}/products/top-stock` |
| 7 | Update franchise name | PATCH | `/api/v1/franchises/{franchiseId}/name` |
| 8 | Update branch name | PATCH | `/api/v1/franchises/{franchiseId}/branches/{branchId}/name` |
| 9 | Update product name | PATCH | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` |

Example — create a franchise:

```bash
curl -X POST http://localhost:8080/api/v1/franchises \
  -H "Content-Type: application/json" \
  -d '{"name":"My Franchise"}'
```

A ready-to-run Postman collection is available under `docs/postman/`.

## Tests and coverage

```bash
./gradlew test
```

Reactive flows are tested with `StepVerifier`. Coverage reports (JaCoCo) are
generated under each module's `build/reports/`.

## Health

- Liveness: `GET /actuator/health/liveness`
- Health: `GET /actuator/health`

## Deployment (AWS)

Infrastructure as Code lives in a separate repository (`franchise-api-infra`).
It deploys the app to AWS with ECS Fargate + ECR + ALB + Secrets Manager using
Terraform. See that repository's `DEPLOY.md` for the step-by-step guide.

## Architecture

Clean Architecture (Bancolombia scaffold), organized as Gradle modules:

- `domain/model` — entities, ports (in/out), domain exceptions
- `domain/usecase` — business use cases (reactive)
- `infrastructure/driven-adapters/mongo-repository` — reactive MongoDB adapter
- `infrastructure/entry-points/reactive-web` — WebFlux router, handlers, DTOs
- `applications/app-service` — Spring Boot bootstrap and wiring

Business rules live only in the domain; ports are interfaces in the domain and
adapters implement them in infrastructure.
