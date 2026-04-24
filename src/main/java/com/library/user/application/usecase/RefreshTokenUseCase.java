package com.library.user.application.usecase;

import com.library.user.application.dto.AuthTokens;
import com.library.user.application.exception.NotFoundException;
import com.library.user.application.exception.UnauthorizedException;
import com.library.user.application.port.RefreshTokenRepository;
import com.library.user.domain.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthTokenIssuer authTokenIssuer;
    private final Clock clock;

    public RefreshTokenUseCase(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            AuthTokenIssuer authTokenIssuer,
            Clock clock
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.authTokenIssuer = authTokenIssuer;
        this.clock = clock;
    }

    @Transactional
    public AuthTokens execute(String refreshToken) {
        var now = Instant.now(clock);
        var stored = refreshTokenRepository.findValidByTokenHash(authTokenIssuer.hashToken(refreshToken), now)
                .orElseThrow(() -> new UnauthorizedException("refresh token is invalid"));
        if (stored.isRevoked() || stored.isExpired(now)) {
            throw new UnauthorizedException("refresh token is invalid");
        }
        refreshTokenRepository.revokeByTokenId(stored.tokenId(), now);
        var user = userRepository.findById(stored.userId())
                .orElseThrow(() -> new NotFoundException("user not found"));
        user.assertCanAuthenticate();
        return authTokenIssuer.issue(user, now);
    }
}
