# library-user

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=ARCN-Peach_library-user&token=2fe2b7e5af4cd0f3728ffb39388a9f3374bdd360)](https://sonarcloud.io/summary/new_code?id=ARCN-Peach_library-user)

Microservicio responsable del bounded context **Gestión de Usuarios** dentro del sistema de biblioteca digital.

Gestiona el ciclo de vida del lector y del bibliotecario: registro, autenticación, perfil básico, estado del usuario y reacción a eventos de multas para bloquear o desbloquear acceso.

En integración con `library-fine`, consume hoy:

- `fine.fine.fine_generated.v1` para bloquear usuarios
- `fine.fine.fine_paid.v1` para desbloquear usuarios

**Stack:** Java 21 · Spring Boot 3.x · PostgreSQL · RabbitMQ · Flyway · Maven

---

## Bounded context

| Elemento | Detalle |
| --- | --- |
| Eventos que publica | `UserRegisteredEvent`, `UserBlockedEvent` |
| Eventos que consume | `FineGeneratedEvent`, `FinePaidEvent` |
| Agregado principal | `User` |
| Políticas | Email único · registro público solo `READER` · usuario bloqueado no autentica |

---

## Arquitectura interna — Clean Architecture

```
  HTTP / RabbitMQ
        |
        v
┌───────────────────────────────────────────────┐
│  INTERFACES                                   │
│  AuthController · UserController              │
│  GlobalExceptionHandler · FineEventsListener  │
└──────────────────────┬────────────────────────┘
                       │
                       v
┌───────────────────────────────────────────────┐
│  APPLICATION                                  │
│  RegisterUserUseCase · LoginUseCase           │
│  RefreshTokenUseCase · GetUserProfileUseCase  │
│  UpdateOwnProfileUseCase                      │
│  ChangeUserStatusUseCase                      │
│                                               │
│  PasswordHasher · TokenService                │
│  RefreshTokenRepository · OutboxRepository    │
└──────────┬────────────────────────────────────┘
           │
           v
┌───────────────────────────────────────────────┐
│  DOMAIN                                       │
│  User (Aggregate Root)                        │
│  ├─ UserId · Email · Name                     │
│  ├─ UserRole · UserStatus                     │
│  └─ UserRegisteredEvent · UserBlockedEvent    │
└──────────┬────────────────────────────────────┘
           │ implementado por
           v
┌───────────────────────────────────────────────┐
│  INFRASTRUCTURE                               │
│  PostgresUserRepository                       │
│  PostgresRefreshTokenRepository               │
│  JpaOutboxRepository                          │
│  OutboxPublisher · SecurityConfiguration      │
└───────────────────────────────────────────────┘
```

---

## API REST

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Registrar lector |
| `POST` | `/api/v1/auth/login` | Iniciar sesión |
| `POST` | `/api/v1/auth/refresh` | Renovar tokens |
| `GET` | `/api/v1/users/me` | Consultar perfil propio |
| `PATCH` | `/api/v1/users/me` | Actualizar perfil propio |
| `GET` | `/api/v1/users/{userId}` | Consultar usuario por ID |
| `PATCH` | `/api/v1/users/{userId}/status` | Bloquear o desbloquear usuario |

**Header requerido:** `X-Correlation-Id`

**Documentación interactiva:** `http://localhost:8081/swagger-ui.html`

---

## Levantar con Docker

```bash
docker compose up --build
```

| Servicio | URL |
| --- | --- |
| API | <http://localhost:8081> |
| Swagger UI | <http://localhost:8081/swagger-ui.html> |
| Actuator health | <http://localhost:8081/actuator/health> |
| RabbitMQ Management | <http://localhost:15673> (guest / guest) |
| PostgreSQL | localhost:5433 — db: `library_user` |

---

## Variables principales

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USER`
- `RABBITMQ_PASSWORD`
- `JWT_SECRET`
- `BOOTSTRAP_LIBRARIAN_ENABLED`
- `BOOTSTRAP_LIBRARIAN_NAME`
- `BOOTSTRAP_LIBRARIAN_EMAIL`
- `BOOTSTRAP_LIBRARIAN_PASSWORD`
