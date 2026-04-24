# Contrato HTTP - `library-user` v1

## Servicio owner

- [x] `library-user`

## Endpoints

### `POST /api/v1/auth/register`

- auth requerida: no
- header requerido: `X-Correlation-Id`
- request: `name`, `email`, `password`
- response: perfil creado

### `POST /api/v1/auth/login`

- auth requerida: no
- header requerido: `X-Correlation-Id`
- request: `email`, `password`
- response: `accessToken`, `refreshToken`

### `POST /api/v1/auth/refresh`

- auth requerida: no
- header requerido: `X-Correlation-Id`
- request: `refreshToken`
- response: `accessToken`, `refreshToken`

### `GET /api/v1/users/me`

- auth requerida: JWT
- header requerido: `X-Correlation-Id`

### `PATCH /api/v1/users/me`

- auth requerida: JWT
- header requerido: `X-Correlation-Id`
- request: `name`, `email`

### `GET /api/v1/users/{userId}`

- auth requerida: JWT rol `LIBRARIAN`
- header requerido: `X-Correlation-Id`

### `PATCH /api/v1/users/{userId}/status`

- auth requerida: JWT rol `LIBRARIAN`
- header requerido: `X-Correlation-Id`
- request: `blocked`, `reason`

## Compatibilidad

- version actual: `v1`
- solo cambios aditivos sin romper payloads existentes
