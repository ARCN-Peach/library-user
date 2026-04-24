package com.library.user.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    String eventType();

    String aggregateId();

    Instant occurredAt();

    String correlationId();
}
