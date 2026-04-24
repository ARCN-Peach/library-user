package com.library.user.application.usecase;

import com.library.user.application.dto.AuthTokens;
import com.library.user.application.port.RefreshTokenRepository;
import com.library.user.application.port.TokenService;
import com.library.user.domain.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenIssuer {

    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration refreshTokenTtl = Duration.ofDays(7);

    public AuthTokenIssuer(TokenService tokenService, RefreshTokenRepository refreshTokenRepository) {
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthTokens issue(User user, Instant now) {
        var accessToken = tokenService.createAccessToken(user.id(), user.role(), now);
        var refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        refreshTokenRepository.save(new RefreshTokenRepository.NewRefreshToken(
                UUID.randomUUID(),
                user.id(),
                hashToken(refreshToken),
                now.plus(refreshTokenTtl),
                now
        ));
        return new AuthTokens(accessToken, refreshToken);
    }

    public String hashToken(String refreshToken) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
