# Url-Shorten

A URL shortener REST API built with Spring Boot. Stores mappings in PostgreSQL, caches lookups in Redis, and manages schema migrations with Liquibase. Ships with a Docker Compose stack for local development and an interactive CLI runner — the primary way to exercise the API without needing a separate HTTP client like curl or Postman.

## Features

- Create, retrieve, update, and delete shortened URLs
- Base62-encoded short codes generated from the entity's sequential ID
- Redis-backed caching (`@Cacheable` / `@CachePut` / `@CacheEvict`) on lookup, update, and create
- Access-count tracking per short URL
- Paginated listing of all shortened URLs
- Centralized error handling via `@RestControllerAdvice`
- Liquibase-managed schema migrations
- Dockerized app + PostgreSQL + Redis via Docker Compose
- Built-in interactive CLI, started automatically with the server, for making requests against the API without curl or Postman

## Tech Stack

| Layer          | Technology                              |
|----------------|------------------------------------------|
| Language       | Java 26                                   |
| Framework      | Spring Boot 4.1 (Web MVC, Data JPA, Cache, Data Redis) |
| Database       | PostgreSQL                                |
| Cache          | Redis                                     |
| Migrations     | Liquibase                                 |
| Mapping        | ModelMapper                               |
| Build Tool     | Gradle (Kotlin DSL)                       |
| Testing        | JUnit 5, Spring Boot Test, Testcontainers |
| Containerization | Docker, Docker Compose                  |

## Getting Started

### Prerequisites

- Docker
- JDK 26 (only needed if running outside Docker)

### Run with Docker Compose (recommended)

1. Copy the example environment file and fill in values:

   ```bash
   cp .env.example .env
   ```

   ```dotenv
   POSTGRES_DB=mydatabase
   POSTGRES_USER=your_user
   POSTGRES_PASSWORD=your_password
   ```

2. Build and start the stack (app + PostgreSQL + Redis):

   ```bash
   docker compose up --build
   ```

3. The API is available at `http://localhost:8080`, but the primary way to interact with it is the interactive CLI attached to the `backend` container's stdin/stdout. Run Compose in the foreground (not `-d`) so the CLI prompt (`>`) shows up directly in your terminal:

   ```bash
   docker compose up --build backend
   ```

   If the stack is already running detached, attach to the CLI with:

   ```bash
   docker attach url-shorten-backend-1
   ```

   See [CLI Runner](#cli-runner) below for usage.

### Run locally without Docker

1. Start PostgreSQL and Redis yourself (or run `docker compose up postgres redis`).
2. Update `src/main/resources/application.properties` with your local connection details.
3. Run the app:

   ```bash
   ./gradlew bootRun
   ```

## Configuration

Environment variables consumed by `compose.yaml`:

| Variable              | Description                          |
|------------------------|---------------------------------------|
| `POSTGRES_DB`          | PostgreSQL database name             |
| `POSTGRES_USER`        | PostgreSQL username                  |
| `POSTGRES_PASSWORD`    | PostgreSQL password                  |
| `SPRING_DATA_REDIS_HOST` | Redis host (set to `redis` in Compose) |
| `SPRING_DATA_REDIS_PORT` | Redis port (`6379`)                |

These are defined in `.env` (see `.env.example` for the template) and are picked up automatically by `docker compose up`.

## CLI Runner

The application starts an interactive CLI alongside the HTTP server (`ApiCliRunner`) — this is the primary, recommended way to interact with the API during development, since it needs no separate HTTP client. It reads `METHOD /path` commands from stdin and issues the corresponding request against the local server.

```
=== API CLI ready. Example: PUT /shorten/1 ===
Type 'exit' to stop the CLI (server keeps running).
> POST /shorten
{
  "url": "https://www.example.com/some/long/url"
}
> GET /shorten/n
> exit
```

For `PUT`, `POST`, and `PATCH`, paste a JSON body on the following line(s) after the command; it's captured until braces balance. Type `exit` to stop the CLI (the server keeps running). See [Getting Started](#getting-started) for how to reach the prompt when running via Docker Compose.

## API Reference

Base path: `/shorten`. Every endpoint below is shown as a CLI command — type it at the `>` prompt (see [CLI Runner](#cli-runner)). A [curl equivalent](#using-curl-instead) is also provided for tooling that needs raw HTTP.

| Method | Endpoint              | Description                          |
|--------|------------------------|---------------------------------------|
| POST   | `/shorten`             | Create a new shortened URL            |
| GET    | `/shorten`             | List all shortened URLs (paginated)   |
| GET    | `/shorten/{shortUrl}`  | Retrieve a URL by its short code      |
| PUT    | `/shorten/{shortUrl}`  | Update an existing shortened URL      |
| DELETE | `/shorten/{shortUrl}`  | Delete a shortened URL                |
| GET    | `/shorten/{shortUrl}/stats` | Retrieve access stats for a short URL |

### Create a short URL

```
> POST /shorten
{
  "url": "https://www.example.com/some/long/url"
}
```

**Response** `201 Created`

```json
{
  "id": 1,
  "url": "https://www.example.com/some/long/url",
  "shortURL": "n",
  "createdAt": "2026-08-29T09:20:52.027583",
  "updatedAt": null,
  "accessCount": 0
}
```

### Retrieve a short URL

```
> GET /shorten/n
```

### List all URLs

```
> GET /shorten?page=0&size=20
```

### Update a short URL

```
> PUT /shorten/n
{
  "url": "https://www.example.com/updated"
}
```

### Delete a short URL

```
> DELETE /shorten/n
```

### Get stats for a short URL

```
> GET /shorten/n/stats
```

### Error format

Errors are returned as JSON with a consistent shape:

```json
{
  "timestamp": "2026-08-29T09:21:04.578446168Z",
  "status": 404,
  "message": "Url not found with shortUrl: n",
  "path": "/shorten/n"
}
```

### Using curl instead

If you're scripting against the API or don't have access to the container's stdin (e.g. CI), the same requests work with any HTTP client:

```bash
# Create
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.example.com/some/long/url"}'

# Retrieve
curl http://localhost:8080/shorten/n

# List
curl "http://localhost:8080/shorten?page=0&size=20"

# Update
curl -X PUT http://localhost:8080/shorten/n \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.example.com/updated"}'

# Delete
curl -X DELETE http://localhost:8080/shorten/n

# Stats
curl http://localhost:8080/shorten/n/stats
```

## Caching

Lookups (`GET /shorten/{shortUrl}`) are cached in Redis under the `URL_CACHE` cache with a 10-minute TTL, keyed by short code. Cache entries are written on create/update (`@CachePut`) and removed on delete (`@CacheEvict`), so reads stay consistent with the database without re-querying on every request.

## Database Schema

Migrations are managed with Liquibase (`src/main/resources/db/changelog`). The core table:

**`url_entity`**

| Column        | Type          | Notes                          |
|---------------|---------------|----------------------------------|
| `id`          | BIGINT        | Primary key, sequence-generated  |
| `url`         | VARCHAR(300)  | Original URL, unique, not null   |
| `short_url`   | VARCHAR(100)  | Base62-encoded short code        |
| `created_at`  | TIMESTAMP     | Set on insert                    |
| `updated_at`  | TIMESTAMP     | Set on update                    |
| `access_count`| INTEGER       | Defaults to 0                    |

## Testing

Run the test suite (includes Testcontainers-based integration tests for PostgreSQL and Redis, requires Docker):

```bash
./gradlew test
```

## Project Structure

```
src/main/java/com/berk/urlshorten
├── cli/            # Interactive CLI runner
├── configs/         # Redis cache configuration
├── controllers/      # REST controllers
├── domain/
│   ├── dto/          # API request/response DTOs
│   └── entities/      # JPA entities
├── exceptions/        # Custom exceptions + global error handling
├── repository/        # Spring Data JPA repositories
└── sevices/            # Service layer + implementations
```
