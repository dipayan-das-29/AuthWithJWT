# admin-be

Sample Spring Boot 3.4 (Spring Security 6.4) project on Java 21 with Maven and PostgreSQL.

## Requirements
- JDK 21
- Maven 3.9+ (or add the Maven wrapper with `mvn wrapper:wrapper`)
- PostgreSQL 16 (or `docker compose up -d`)

## Configuration
Defaults are in `src/main/resources/application.properties`. Override with environment variables:
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Run
```
docker compose up -d
mvn spring-boot:run
```

## Test
Tests use an in-memory H2 database (PostgreSQL mode), so no database is needed:
```
mvn test
```

## Endpoints
| Endpoint | Access |
|---|---|
| `GET /api/public/hello` | public |
| `POST /api/public/register` | public, body `{"username": "...", "password": "..."}` (password 8+ chars) |
| `GET /api/user/me` | USER or ADMIN (HTTP Basic) |
| `GET /api/admin/dashboard` | ADMIN (HTTP Basic) |

Seeded users: `user / user123` (USER), `admin / admin123` (ADMIN). Change or remove them in `DataSeeder` before real use.

## Notes
- Users are stored in the `app_users` table, passwords are BCrypt-hashed.
- `spring.jpa.hibernate.ddl-auto=update` is for demo use. Use Flyway or Liquibase in production.
