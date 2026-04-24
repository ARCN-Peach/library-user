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

### `fine.fine.fine_generated.v1`

- exchange: `fine`
- routing key: `fine.fine.fine_generated.v1`
- efecto: bloquea usuario

### `fine.fine.fine_paid.v1`

- exchange: `fine`
- routing key: `fine.fine.fine_paid.v1`
- efecto: desbloquea usuario

Nota de alineación con `library-fine`:

- `library-fine` hoy publica `FinePaidEvent`, no un evento explícito de "usuario sin deuda pendiente".
- `library-user` consume ese evento para desbloqueo y queda alineado técnicamente con `library-fine`.
- Si en el futuro `library-fine` soporta múltiples multas abiertas por usuario, conviene reemplazar este contrato por un evento de deuda liquidada o agregar una política adicional aguas arriba.
