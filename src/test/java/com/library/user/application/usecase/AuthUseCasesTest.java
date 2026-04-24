package com.library.user.application.usecase;

import com.library.user.application.exception.NotFoundException;
import com.library.user.application.exception.UnauthorizedException;
import com.library.user.application.port.RefreshTokenRepository;
import com.library.user.application.port.TokenService;
import com.library.user.domain.model.Email;
import com.library.user.domain.model.User;
import com.library.user.domain.model.UserId;
import com.library.user.domain.model.UserRole;
import com.library.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUseCasesTest {

    private RegisterUserUseCaseTest.InMemoryUserRepository userRepository;
    private InMemoryRefreshTokenRepository refreshTokenRepository;
    private AuthTokenIssuer authTokenIssuer;
    private LoginUseCase loginUseCase;
    private RefreshTokenUseCase refreshTokenUseCase;

    @BeforeEach
    void setUp() {
        userRepository = new RegisterUserUseCaseTest.InMemoryUserRepository();
        refreshTokenRepository = new InMemoryRefreshTokenRepository();
        var tokenService = new TokenService() {
            @Override
            public String createAccessToken(UserId userId, UserRole role, Instant issuedAt) {
                return "token-" + userId.value();
            }

            @Override
            public AccessTokenPayload parseAccessToken(String token) {
                throw new UnsupportedOperationException();
            }
        };
        authTokenIssuer = new AuthTokenIssuer(tokenService, refreshTokenRepository);
        var passwordHasher = new com.library.user.application.port.PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return "hashed-" + rawPassword;
            }

            @Override
            public boolean matches(String rawPassword, String hashedPassword) {
                return hashedPassword.equals("hashed-" + rawPassword);
            }
        };
        var clock = Clock.fixed(Instant.parse("2026-04-23T00:00:00Z"), ZoneOffset.UTC);
        loginUseCase = new LoginUseCase(userRepository, passwordHasher, authTokenIssuer, clock);
        refreshTokenUseCase = new RefreshTokenUseCase(refreshTokenRepository, userRepository, authTokenIssuer, clock);
    }

    @Test
    void login_issues_access_and_refresh_tokens() {
        var user = User.registerReader(
                new com.library.user.domain.model.Name("Reader"),
                new Email("reader@test.com"),
                "hashed-Password123",
                Instant.parse("2026-04-23T00:00:00Z")
        );
        userRepository.save(user);

        var tokens = loginUseCase.execute("reader@test.com", "Password123");

        assertThat(tokens.accessToken()).startsWith("token-");
        assertThat(tokens.refreshToken()).isNotBlank();
    }

    @Test
    void login_rejects_invalid_password() {
        var user = User.registerReader(
                new com.library.user.domain.model.Name("Reader"),
                new Email("reader@test.com"),
                "hashed-Password123",
                Instant.parse("2026-04-23T00:00:00Z")
        );
        userRepository.save(user);

        assertThatThrownBy(() -> loginUseCase.execute("reader@test.com", "wrong"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_rotates_token() {
        var user = User.registerReader(
                new com.library.user.domain.model.Name("Reader"),
                new Email("reader@test.com"),
                "hashed-Password123",
                Instant.parse("2026-04-23T00:00:00Z")
        );
        userRepository.save(user);
        var issued = authTokenIssuer.issue(user, Instant.parse("2026-04-23T00:00:00Z"));

        var refreshed = refreshTokenUseCase.execute(issued.refreshToken());

        assertThat(refreshed.accessToken()).startsWith("token-");
        assertThat(refreshed.refreshToken()).isNotEqualTo(issued.refreshToken());
    }

    @Test
    void refresh_fails_when_user_missing() {
        var issued = refreshTokenRepository.issueDetachedToken(authTokenIssuer, UserId.newId(), Instant.parse("2026-04-23T00:00:00Z"));

        assertThatThrownBy(() -> refreshTokenUseCase.execute(issued))
                .isInstanceOf(NotFoundException.class);
    }

    static class InMemoryRefreshTokenRepository implements RefreshTokenRepository {
        private final Map<UUID, StoredRefreshToken> store = new HashMap<>();

        @Override
        public StoredRefreshToken save(NewRefreshToken token) {
            var stored = new StoredRefreshToken(token.tokenId(), token.userId(), token.tokenHash(), token.expiresAt(), null, token.createdAt());
            store.put(token.tokenId(), stored);
            return stored;
        }

        @Override
        public Optional<StoredRefreshToken> findValidByTokenHash(String tokenHash, Instant now) {
            return store.values().stream()
                    .filter(token -> token.tokenHash().equals(tokenHash) && !token.isExpired(now) && !token.isRevoked())
                    .findFirst();
        }

        @Override
        public void revokeByTokenId(UUID tokenId, Instant revokedAt) {
            var stored = store.get(tokenId);
            if (stored != null) {
                store.put(tokenId, new StoredRefreshToken(
                        stored.tokenId(), stored.userId(), stored.tokenHash(), stored.expiresAt(), revokedAt, stored.createdAt()
                ));
            }
        }

        String issueDetachedToken(AuthTokenIssuer issuer, UserId userId, Instant now) {
            var refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
            save(new NewRefreshToken(UUID.randomUUID(), userId, issuer.hashToken(refreshToken), now.plusSeconds(3600), now));
            return refreshToken;
        }
    }
}
