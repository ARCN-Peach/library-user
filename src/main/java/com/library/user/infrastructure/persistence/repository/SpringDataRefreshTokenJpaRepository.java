package com.library.user.infrastructure.persistence.repository;

import com.library.user.infrastructure.persistence.entity.RefreshTokenEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataRefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    @Query("select r from RefreshTokenEntity r where r.tokenHash = :tokenHash and r.revokedAt is null and r.expiresAt > :now")
    Optional<RefreshTokenEntity> findValidByTokenHash(String tokenHash, Instant now);

    @Modifying
    @Query("update RefreshTokenEntity r set r.revokedAt = :revokedAt where r.tokenId = :tokenId")
    void revokeByTokenId(UUID tokenId, Instant revokedAt);
}
