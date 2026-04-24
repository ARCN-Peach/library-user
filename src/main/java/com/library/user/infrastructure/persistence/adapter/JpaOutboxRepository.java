package com.library.user.infrastructure.persistence.adapter;

import com.library.user.application.port.OutboxRepository;
import com.library.user.domain.event.DomainEvent;
import com.library.user.infrastructure.persistence.entity.OutboxEventEntity;
import com.library.user.infrastructure.persistence.repository.SpringDataOutboxJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaOutboxRepository implements OutboxRepository {

    private final SpringDataOutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public JpaOutboxRepository(SpringDataOutboxJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(DomainEvent event) {
        var entity = new OutboxEventEntity();
        entity.setEventId(event.eventId().toString());
        entity.setEventType(event.eventType());
        entity.setAggregateId(event.aggregateId());
        entity.setCorrelationId(event.correlationId());
        entity.setPayload(serialize(event));
        entity.setStatus("PENDING");
        entity.setCreatedAt(event.occurredAt());
        repository.save(entity);
    }

    @Override
    public List<OutboxMessage> findPending(int limit) {
        return repository.findTop50ByStatusOrderByIdAsc("PENDING").stream()
                .limit(limit)
                .map(entity -> new OutboxMessage(
                        entity.getId(),
                        entity.getEventId(),
                        entity.getEventType(),
                        entity.getAggregateId(),
                        entity.getCorrelationId(),
                        entity.getPayload()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void markPublished(long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setStatus("PUBLISHED");
            entity.setPublishedAt(Instant.now());
            entity.setLastError(null);
            repository.save(entity);
        });
    }

    @Override
    @Transactional
    public void markFailed(long id, String error) {
        repository.findById(id).ifPresent(entity -> {
            entity.setStatus("FAILED");
            entity.setLastError(error);
            repository.save(entity);
        });
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("cannot serialize domain event", ex);
        }
    }
}
