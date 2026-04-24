package com.library.user.domain.event;

import com.library.user.domain.model.User;
import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String correlationId,
        String email,
        String name,
        String role
) implements DomainEvent {

    public static UserRegisteredEvent from(User user, Instant occurredAt, String correlationId) {
        return new UserRegisteredEvent(
                UUID.randomUUID(),
                "user.registered.v1",
                user.id().value().toString(),
                occurredAt,
                correlationId,
                user.email().value(),
                user.name().value(),
                user.role().name()
        );
    }
}
