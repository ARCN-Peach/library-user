# Contratos de eventos - `library-user` v1

## Publicados

### `user.registered.v1`

- exchange: `library.user.exchange`
- routing key: `user.registered.v1`
- payload:
  - `eventId`
  - `eventType`
  - `aggregateId`
  - `occurredAt`
  - `correlationId`
  - `email`
  - `name`
  - `role`

### `user.blocked.v1`

- exchange: `library.user.exchange`
- routing key: `user.blocked.v1`
- payload:
  - `eventId`
  - `eventType`
  - `aggregateId`
  - `occurredAt`
  - `correlationId`
  - `reason`

## Consumidos

### `fine.generated.v1`

- exchange: `library.fine.exchange`
- routing key: `fine.generated.v1`
- efecto: bloquea usuario

### `user.debt-cleared.v1`

- exchange: `library.fine.exchange`
- routing key: `user.debt-cleared.v1`
- efecto: desbloquea usuario
