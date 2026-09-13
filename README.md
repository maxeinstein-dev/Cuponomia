# Cuponomia Microservices

Cuponomia is a coupon platform built as a small event-driven microservices system. It separates coupon administration from checkout validation, publishes coupon changes through Kafka, and keeps a local coupon projection in the validation service so checkout can stay fast and resilient.

## Live Demo Flow

The project is designed to be explored through Swagger:

- Production Swagger target: `https://cuponomia.maxsueleinstein.dev`
- Management API Swagger: `http://localhost:8081/swagger-ui.html`
- Validation API Swagger: `http://localhost:8082/swagger-ui.html`
- Root redirects: `http://localhost:8081/` and `http://localhost:8082/` open each service's Swagger UI.
- Recruiter guide: [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md)
- The published Swagger includes links back to this README and the demo guide, so reviewers can move between the live API and documentation quickly.

### Production Demo Constraint

The repository keeps the architecture split into management and validation services. The published Render demo intentionally exposes a single Swagger from `coupon-management-service` because the free plan currently has one web service and one PostgreSQL database available for this project. To keep the recruiter demo usable inside that constraint, the management service also exposes a demo checkout endpoint in the same Swagger. Kafka can be disabled in this environment with `CUPONOMIA_KAFKA_ENABLED=false`.

Both Swagger UIs include ready-to-run request examples. The quickest path is:

1. Open the management Swagger and inspect or create a coupon.
2. Apply `MAX50` at checkout from the same published Swagger.
3. Compare successful, invalid, inactive, and expired coupon scenarios.

## What This Project Demonstrates

- Domain-first design with coupon rules isolated from Spring and persistence.
- Two Spring Boot services with independent databases.
- Kafka-based event propagation from coupon management to checkout validation.
- Local read model/projection in the validation service.
- Swagger examples and a demo guide aimed at hands-on technical review.
- Resilience work around checkout timeout behavior.

## Modules

| Module | Responsibility |
| --- | --- |
| `coupon-management-service` | Creates, lists, retrieves, and deactivates coupons. Persists the source of truth and publishes coupon change events. |
| `coupon-validation-service` | Applies coupons during checkout using its own local projection and coupon usage records. |
| `coupon-contracts` | Shared Kafka event contracts between services. |

## Architecture

Each service follows a layered structure:

- `interfaces`: REST controllers and HTTP exception handlers.
- `application`: use cases, DTOs, mappers, and application ports.
- `domain`: coupon model, value objects, validation rules, and domain exceptions.
- `infrastructure`: JPA persistence, Kafka integration, OpenAPI configuration, and adapters.

```text
.
|-- coupon-contracts
|   `-- src/main/java/.../contracts/event
|-- coupon-management-service
|   |-- src/main/java/.../application
|   |-- src/main/java/.../domain
|   |-- src/main/java/.../infrastructure
|   `-- src/main/java/.../interfaces
|-- coupon-validation-service
|   |-- src/main/java/.../application
|   |-- src/main/java/.../domain
|   |-- src/main/java/.../infrastructure
|   `-- src/main/java/.../interfaces
|-- docs
|   `-- DEMO_GUIDE.md
|-- docker-compose.yml
|-- Dockerfile
`-- pom.xml
```

## Stack

- Java 25
- Spring Boot 4.0.6
- Maven multi-module build
- Kafka
- PostgreSQL per service in Docker
- H2 for local/test profiles where applicable
- Springdoc OpenAPI / Swagger UI

## Run Locally

Start the full stack:

```bash
docker compose up --build
```

Services:

- Management API: `http://localhost:8081`
- Validation API: `http://localhost:8082`
- Kafka: `localhost:9092`
- Management Postgres: `localhost:5432`, database `coupon_management`
- Validation Postgres: `localhost:5433`, database `coupon_validation`

Run all tests:

```bash
./mvnw test
```

Run one module locally:

```bash
./mvnw -pl coupon-management-service -am spring-boot:run
./mvnw -pl coupon-validation-service -am spring-boot:run
```

## Render Demo Environment

Use these variables for the single Swagger demo:

```env
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=10000
JAVA_OPTS=-Xmx512m
MODULE=coupon-management-service
CUPONOMIA_KAFKA_ENABLED=false
SPRING_DATASOURCE_URL=jdbc:postgresql://<internal-database-host>:5432/<database-name>
SPRING_DATASOURCE_USERNAME=<database-user>
SPRING_DATASOURCE_PASSWORD=<database-password>
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

`SPRING_DATASOURCE_URL` must include the `jdbc:` prefix. Render's internal PostgreSQL URL usually needs to be converted from `postgresql://...` to `jdbc:postgresql://...`.

## Main Endpoints

Management service, port `8081`:

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/v1/coupons` | Create coupon |
| `GET` | `/api/v1/coupons` | List coupons |
| `GET` | `/api/v1/coupons/{code}` | Get coupon by code |
| `PATCH` | `/api/v1/coupons/{code}/deactivate` | Deactivate coupon |

Single Swagger demo endpoint, exposed by the management service in production:

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/v1/checkout/apply-coupon` | Validate and apply a coupon for the free Render demo |

Validation service, port `8082`:

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/v1/checkout/apply-coupon` | Validate and apply coupon |

## Example Requests

Create a coupon:

```bash
curl -X POST http://localhost:8081/api/v1/coupons \
  -H "Content-Type: application/json" \
  -d '{
    "code": "RECRUITER50",
    "description": "50% discount for a recruiter demo checkout.",
    "discountType": "PERCENTAGE",
    "discountValue": 50,
    "rules": {
      "minimumOrderValue": 16000.00,
      "expiresAt": "2027-12-31T23:59:59",
      "singleUsePerClient": true,
      "maxUsages": 10
    }
  }'
```

Apply the seeded `MAX50` coupon:

```bash
curl -X POST http://localhost:8082/api/v1/checkout/apply-coupon \
  -H "Content-Type: application/json" \
  -d '{
    "couponCode": "MAX50",
    "clientId": "recruiter-demo",
    "orderTotal": 16000.00
  }'
```

## Development Notes

The repository includes a pre-commit hook that runs affected tests before accepting commits.

Enable it after cloning:

```bash
git config core.hooksPath .githooks
```

On Windows:

```powershell
./scripts/install-git-hooks.ps1
```

More context on design decisions is available in [DESENVOLVIMENTO.md](DESENVOLVIMENTO.md).

## Author

Built by [Maxsuel Einstein](https://github.com/maxeinstein-dev).
