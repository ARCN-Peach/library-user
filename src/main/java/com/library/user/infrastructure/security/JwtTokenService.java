package com.library.user.infrastructure.security;

import com.library.user.application.port.TokenService;
import com.library.user.domain.model.UserId;
import com.library.user.domain.model.UserRole;
import com.library.user.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService implements TokenService {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String createAccessToken(UserId userId, UserRole role, Instant issuedAt) {
        return Jwts.builder()
                .subject(userId.value().toString())
                .claim("role", role.name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plusSeconds(properties.accessTokenTtlSeconds())))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public AccessTokenPayload parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AccessTokenPayload(
                new UserId(java.util.UUID.fromString(claims.getSubject())),
                UserRole.valueOf(claims.get("role", String.class))
        );
    }
}
