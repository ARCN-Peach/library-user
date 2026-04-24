package com.library.user.infrastructure.persistence.adapter;

import com.library.user.application.port.RefreshTokenRepository;
import com.library.user.domain.model.UserId;
import com.library.user.infrastructure.persistence.entity.RefreshTokenEntity;
import com.library.user.infrastructure.persistence.repository.SpringDataRefreshTokenJpaRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenJpaRepository repository;

    public PostgresRefreshTokenRepository(SpringDataRefreshTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public StoredRefreshToken save(NewRefreshToken token) {
        var entity = new RefreshTokenEntity();
        entity.setTokenId(token.tokenId());
        entity.setUserId(token.userId().value());
        entity.setTokenHash(token.tokenHash());
        entity.setExpiresAt(token.expiresAt());
        entity.setCreatedAt(token.createdAt());
        entity.setRevokedAt(null);
        var saved = repository.save(entity);
        return toStored(saved);
    }

    @Override
    public Optional<StoredRefreshToken> findValidByTokenHash(String tokenHash, Instant now) {
        return repository.findValidByTokenHash(tokenHash, now).map(this::toStored);
    }

    @Override
    public void revokeByTokenId(java.util.UUID tokenId, Instant revokedAt) {
        repository.revokeByTokenId(tokenId, revokedAt);
    }

    private StoredRefreshToken toStored(RefreshTokenEntity entity) {
        return new StoredRefreshToken(
                entity.getTokenId(),
                new UserId(entity.getUserId()),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getCreatedAt()
        );
    }
}
