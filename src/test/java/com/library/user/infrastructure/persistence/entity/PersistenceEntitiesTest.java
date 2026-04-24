package com.library.user.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PersistenceEntitiesTest {

    @Test
    void userEntityGettersAndSettersWork() {
        var entity = new UserEntity();
        var now = Instant.parse("2026-04-23T00:00:00Z");
        var id = UUID.randomUUID();

        entity.setId(id);
        entity.setName("Reader");
        entity.setEmail("reader@test.com");
        entity.setPasswordHash("hash");
        entity.setRole("READER");
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Reader");
        assertThat(entity.getEmail()).isEqualTo("reader@test.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hash");
        assertThat(entity.getRole()).isEqualTo("READER");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void outboxEventEntityGettersAndSettersWork() {
        var entity = new OutboxEventEntity();
        var now = Instant.parse("2026-04-23T00:00:00Z");

        entity.setId(10L);
        entity.setEventId("evt-1");
        entity.setEventType("user.blocked.v1");
        entity.setAggregateId("agg-1");
        entity.setCorrelationId("corr-1");
        entity.setPayload("{\"id\":1}");
        entity.setStatus("PENDING");
        entity.setCreatedAt(now);
        entity.setPublishedAt(now);
        entity.setLastError("none");

        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getEventId()).isEqualTo("evt-1");
        assertThat(entity.getEventType()).isEqualTo("user.blocked.v1");
        assertThat(entity.getAggregateId()).isEqualTo("agg-1");
        assertThat(entity.getCorrelationId()).isEqualTo("corr-1");
        assertThat(entity.getPayload()).isEqualTo("{\"id\":1}");
        assertThat(entity.getStatus()).isEqualTo("PENDING");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getPublishedAt()).isEqualTo(now);
        assertThat(entity.getLastError()).isEqualTo("none");
    }

    @Test
    void refreshTokenEntityGettersAndSettersWork() {
        var entity = new RefreshTokenEntity();
        var now = Instant.parse("2026-04-23T00:00:00Z");
        var tokenId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        entity.setTokenId(tokenId);
        entity.setUserId(userId);
        entity.setTokenHash("hash");
        entity.setExpiresAt(now.plusSeconds(600));
        entity.setRevokedAt(now.plusSeconds(60));
        entity.setCreatedAt(now);

        assertThat(entity.getTokenId()).isEqualTo(tokenId);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getTokenHash()).isEqualTo("hash");
        assertThat(entity.getExpiresAt()).isEqualTo(now.plusSeconds(600));
        assertThat(entity.getRevokedAt()).isEqualTo(now.plusSeconds(60));
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }
}
