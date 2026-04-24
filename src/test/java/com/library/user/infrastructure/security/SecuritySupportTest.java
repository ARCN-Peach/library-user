package com.library.user.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.library.user.domain.model.UserId;
import com.library.user.domain.model.UserRole;
import com.library.user.infrastructure.config.JwtProperties;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SecuritySupportTest {

    @Test
    void bCryptHasherHashesAndMatches() {
        var hasher = new BCryptPasswordHasher();

        var hash = hasher.hash("Password123");

        assertThat(hash).isNotBlank();
        assertThat(hasher.matches("Password123", hash)).isTrue();
        assertThat(hasher.matches("wrong", hash)).isFalse();
    }

    @Test
    void jwtTokenServiceCreatesAndParsesToken() {
        var service = new JwtTokenService(new JwtProperties(
                "this-is-a-very-long-secret-for-hmac-signing-1234567890",
                900
        ));
        var userId = UserId.newId();
        var issuedAt = Instant.now();

        var token = service.createAccessToken(userId, UserRole.LIBRARIAN, issuedAt);
        var payload = service.parseAccessToken(token);

        assertThat(payload.userId()).isEqualTo(userId);
        assertThat(payload.role()).isEqualTo(UserRole.LIBRARIAN);
    }

    @Test
    void jwtTokenServiceRejectsInvalidToken() {
        var service = new JwtTokenService(new JwtProperties(
                "this-is-a-very-long-secret-for-hmac-signing-1234567890",
                900
        ));

        assertThatThrownBy(() -> service.parseAccessToken("invalid-token"))
                .isInstanceOf(RuntimeException.class);
    }
}
