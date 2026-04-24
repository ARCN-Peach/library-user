package com.library.user.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.library.user.domain.model.Email;
import com.library.user.domain.model.Name;
import com.library.user.domain.model.User;
import com.library.user.infrastructure.persistence.entity.UserEntity;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toEntityMapsDomainFields() {
        var now = Instant.parse("2026-04-23T00:00:00Z");
        var user = User.registerReader(new Name("Reader"), new Email("reader@test.com"), "hash", now);

        var entity = mapper.toEntity(user);

        assertThat(entity.getId()).isEqualTo(user.id().value());
        assertThat(entity.getName()).isEqualTo("Reader");
        assertThat(entity.getEmail()).isEqualTo("reader@test.com");
        assertThat(entity.getRole()).isEqualTo("READER");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void toDomainMapsEntityFields() {
        var now = Instant.parse("2026-04-23T00:00:00Z");
        var entity = new UserEntity();
        entity.setId(java.util.UUID.randomUUID());
        entity.setName("Reader");
        entity.setEmail("reader@test.com");
        entity.setPasswordHash("hash");
        entity.setRole("READER");
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        var user = mapper.toDomain(entity);

        assertThat(user.id().value()).isEqualTo(entity.getId());
        assertThat(user.name().value()).isEqualTo("Reader");
        assertThat(user.email().value()).isEqualTo("reader@test.com");
        assertThat(user.passwordHash()).isEqualTo("hash");
    }
}
