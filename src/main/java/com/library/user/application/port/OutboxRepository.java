package com.library.user.application.port;

import com.library.user.domain.event.DomainEvent;
import java.util.List;

public interface OutboxRepository {

    void save(DomainEvent event);

    List<OutboxMessage> findPending(int limit);

    void markPublished(long id);

    void markFailed(long id, String error);

    record OutboxMessage(long id, String eventId, String eventType, String aggregateId, String correlationId, String payload) {}
}
