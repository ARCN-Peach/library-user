# library-user

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=ARCN-Peach_library-user&token=2fe2b7e5af4cd0f3728ffb39388a9f3374bdd360)](https://sonarcloud.io/summary/new_code?id=ARCN-Peach_library-user)

Microservicio responsable del bounded context Gestion de Usuarios dentro del sistema de biblioteca digital.

Gestiona registro y autenticacion de lectores, perfil de usuario, refresco de tokens, administracion de estado (activo/bloqueado) y reaccion a eventos de multas para bloquear o desbloquear acceso.

Stack: Java 21, Spring Boot 3.4, PostgreSQL, RabbitMQ, Flyway, Maven.

---

## Bounded context

| Elemento | Detalle |
| --- | --- |
| Eventos que publica | UserRegisteredEvent, UserBlockedEvent |
| Eventos que consume | FineGeneratedEvent, FinePaidEvent |
| Agregado principal | User |
| Politicas | Email unico, registro publico solo READER, usuario bloqueado no autentica |

---

## Arquitectura interna (Clean Architecture)

```
  HTTP / RabbitMQ
        |
        v
┌────────────────────────────────────────────────┐
│ INTERFACES                                     │
│ AuthController · UserController                │
│ GlobalExceptionHandler · FineEventsListener    │
│ CorrelationIdFilter                            │
└──────────────────────┬─────────────────────────┘
                       │
                       v
┌────────────────────────────────────────────────┐
│ APPLICATION                                    │
│ RegisterUserUseCase · LoginUseCase             │
│ RefreshTokenUseCase · GetUserProfileUseCase    │
│ UpdateOwnProfileUseCase · ChangeUserStatus...  │
│ HandleFineGeneratedUseCase · HandleFinePaid... │
│ BootstrapLibrarianUseCase                      │
└──────────────────────┬─────────────────────────┘
                       │
                       v
┌────────────────────────────────────────────────┐
│ DOMAIN                                         │
│ User (Aggregate Root)                          │
│ UserId · Name · Email · UserRole · UserStatus  │
│ UserRegisteredEvent · UserBlockedEvent         │
└──────────────────────┬─────────────────────────┘
                       │ implementado por
                       v
┌────────────────────────────────────────────────┐
│ INFRASTRUCTURE                                 │
│ PostgresUserRepository                         │
│ PostgresRefreshTokenRepository                 │
│ JpaOutboxRepository · OutboxPublisher          │
│ RabbitMqConfiguration · SecurityConfiguration  │
└────────────────────────────────────────────────┘
```

Regla principal: las dependencias apuntan hacia adentro. Dominio no depende de frameworks.

---

## Contexto de integracion

Consume eventos de multas desde el exchange fine:

- fine.fine.fine_generated.v1 -> bloquea usuario
- fine.fine.fine_paid.v1 -> desbloquea usuario

Publica eventos de usuario en el exchange library.user.exchange:

- user.registered.v1
- user.blocked.v1

---

## API REST

| Metodo | Endpoint | Descripcion | Auth |
| --- | --- | --- | --- |
| POST | /api/v1/auth/register | Registrar lector | Publico |
| POST | /api/v1/auth/login | Iniciar sesion | Publico |
| POST | /api/v1/auth/refresh | Renovar tokens | Publico |
| GET | /api/v1/users/me | Consultar perfil propio | JWT |
| PATCH | /api/v1/users/me | Actualizar perfil propio | JWT |
| GET | /api/v1/users/{userId} | Consultar usuario por ID | JWT + LIBRARIAN |
| PATCH | /api/v1/users/{userId}/status | Bloquear/desbloquear usuario | JWT + LIBRARIAN |

Header de trazabilidad:

- X-Correlation-Id es opcional para clientes.
- Si no se envia, el servicio genera uno automaticamente y lo retorna en respuesta.

Documentacion interactiva:

- <http://localhost:8081/swagger-ui.html>

Health endpoints:

- <http://localhost:8081/actuator/health>
- <http://localhost:8081/actuator/health/liveness>
- <http://localhost:8081/actuator/health/readiness>

---

## Flujos principales

### 1) Registro de usuario

1. Cliente llama POST /api/v1/auth/register
2. RegisterUserUseCase valida email y reglas de negocio
3. Se persiste user en tabla users
4. Se guarda UserRegisteredEvent en outbox_events
5. OutboxPublisher publica evento por RabbitMQ y marca publicado

### 2) Login y refresh

1. Cliente envia credenciales a POST /api/v1/auth/login
2. LoginUseCase valida usuario activo + password
3. Se genera access token y refresh token
4. Refresh tokens se persisten con hash en refresh_tokens

### 3) Bloqueo/desbloqueo por multas

1. FineEventsListener consume fine_generated -> bloquea usuario
2. FineEventsListener consume fine_paid -> desbloquea usuario
3. Cambio de estado se aplica via ChangeUserStatusUseCase

### 4) Outbox pattern

- Escritura de entidad y outbox en misma transaccion
- Publicacion asincrona con reintento por scheduler
- Si falla RabbitMQ, evento queda pendiente/fallido sin perderse

---

## Levantar en local con Docker

Comandos:

```bash
docker compose up --build
```

Servicios:

| Servicio | URL |
| --- | --- |
| API | <http://localhost:8081> |
| Swagger UI | <http://localhost:8081/swagger-ui.html> |
| Actuator health | <http://localhost:8081/actuator/health> |
| RabbitMQ Management | <http://localhost:15673> (guest/guest) |
| PostgreSQL | localhost:5433 (db: library_user) |

---

## Ejecucion sin Docker

Prerequisitos:

- Java 21
- Maven 3.9+
- PostgreSQL y RabbitMQ accesibles

Comandos:

```bash
mvn spring-boot:run
```

---

## Variables de entorno

| Variable | Default | Descripcion |
| --- | --- | --- |
| PORT | 8081 | Puerto HTTP (Railway inyecta PORT) |
| DB_URL | jdbc:postgresql://localhost:5432/library_user | JDBC URL |
| DB_USER | user_user | Usuario PostgreSQL |
| DB_PASSWORD | postgres | Password PostgreSQL |
| RABBITMQ_HOST | localhost | Host RabbitMQ |
| RABBITMQ_PORT | 5672 | Puerto RabbitMQ |
| RABBITMQ_USER | guest | Usuario RabbitMQ |
| RABBITMQ_PASSWORD | guest | Password RabbitMQ |
| JWT_SECRET | change-me-change-me-change-me-change-me-1234567890 | Secreto JWT (requerido en produccion) |
| BOOTSTRAP_LIBRARIAN_ENABLED | false | Habilita creacion de librarian inicial |
| BOOTSTRAP_LIBRARIAN_NAME | (vacio) | Nombre librarian inicial |
| BOOTSTRAP_LIBRARIAN_EMAIL | (vacio) | Email librarian inicial |
| BOOTSTRAP_LIBRARIAN_PASSWORD | (vacio) | Password librarian inicial |

---

## Base de datos (Flyway)

Migracion actual:

- V1__initial_schema.sql

Tablas principales:

- users
- refresh_tokens
- outbox_events

Notas de esquema:

- users.email tiene restriccion UNIQUE
- refresh_tokens.token_hash tiene restriccion UNIQUE
- outbox_events indexado por (status, id) para polling eficiente

---

## Mensajeria RabbitMQ

Exchanges:

- library.user.exchange (publicacion de eventos de usuario)
- fine (consumo de eventos de multas)

Colas consumidas:

- user.fine-generated (routing key fine.fine.fine_generated.v1)
- user.fine-paid (routing key fine.fine.fine_paid.v1)

Eventos publicados:

- user.registered.v1
- user.blocked.v1

---

## Seguridad y errores

Seguridad:

- API stateless con JWT
- Endpoints publicos: auth/register, auth/login, auth/refresh
- Endpoints de actuator health permitidos
- Endpoints de administracion de usuarios requieren rol LIBRARIAN

Manejo de errores:

- RFC 9457 ProblemDetail
- 400 para validacion y contrato de request
- 401 para autenticacion
- 404 para recursos no encontrados
- 409 para conflictos de negocio

---

## Tests y calidad

Comandos:

```bash
# unit tests
mvn test

# build completo + cobertura JaCoCo (regla minima 80%)
mvn verify
```

Cobertura:

- JaCoCo con threshold minimo 0.80 a nivel bundle

Calidad:

- SonarCloud Quality Gate (badge arriba)

---

## Despliegue en Railway

Checklist:

1. Configurar variables DB_URL, DB_USER, DB_PASSWORD (nombres exactos)
2. Configurar variables RabbitMQ y JWT_SECRET
3. Confirmar que healthcheck apunte a /actuator/health/liveness
4. Confirmar que el servicio usa PORT dinamico (server.port = ${PORT:8081})

Permiso obligatorio en PostgreSQL externo para Flyway:

```sql
GRANT USAGE ON SCHEMA public TO <app_user>;
GRANT CREATE ON SCHEMA public TO <app_user>;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO <app_user>;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO <app_user>;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO <app_user>;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO <app_user>;
```

Sin esos permisos, Flyway puede fallar al arrancar.

---

## Troubleshooting rapido

| Sintoma | Causa probable | Accion |
| --- | --- | --- |
| Healthcheck falla en Railway | Puerto/endpoint incorrecto | Verificar PORT y /actuator/health/liveness |
| Arranque falla en Flyway | Usuario DB sin CREATE en schema public | Aplicar grants del bloque SQL |
| 401 en endpoints privados | JWT ausente o invalido | Reautenticar y enviar Bearer token |
| Usuario no se desbloquea tras pago | Evento fine_paid no llega | Revisar cola user.fine-paid y bindings |

---

## Estado del servicio

Servicio listo para:

- Operacion local con docker-compose
- Integracion asincrona con library-fine
- Despliegue en Railway con PostgreSQL y RabbitMQ administrados
