package com.library.user.application.port;

import com.library.user.domain.model.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    StoredRefreshToken save(NewRefreshToken token);

    Optional<StoredRefreshToken> findValidByTokenHash(String tokenHash, Instant now);

    void revokeByTokenId(UUID tokenId, Instant revokedAt);

    record NewRefreshToken(UUID tokenId, UserId userId, String tokenHash, Instant expiresAt, Instant createdAt) {}

    record StoredRefreshToken(UUID tokenId, UserId userId, String tokenHash, Instant expiresAt, Instant revokedAt, Instant createdAt) {
        public boolean isRevoked() {
            return revokedAt != null;
        }

        public boolean isExpired(Instant now) {
            return expiresAt.isBefore(now);
        }
    }
}
