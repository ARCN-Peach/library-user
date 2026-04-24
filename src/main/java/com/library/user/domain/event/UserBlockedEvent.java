package com.library.user.domain.event;

import com.library.user.domain.model.User;
import java.time.Instant;
import java.util.UUID;

public record UserBlockedEvent(
        UUID eventId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        String correlationId,
        String reason
) implements DomainEvent {

    public static UserBlockedEvent from(User user, Instant occurredAt, String correlationId, String reason) {
        return new UserBlockedEvent(
                UUID.randomUUID(),
                "user.blocked.v1",
                user.id().value().toString(),
                occurredAt,
                correlationId,
                reason
        );
    }
}
