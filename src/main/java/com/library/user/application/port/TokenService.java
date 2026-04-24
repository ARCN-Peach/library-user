package com.library.user.application.port;

import com.library.user.domain.model.UserId;
import com.library.user.domain.model.UserRole;
import java.time.Instant;

public interface TokenService {

    String createAccessToken(UserId userId, UserRole role, Instant issuedAt);

    AccessTokenPayload parseAccessToken(String token);

    record AccessTokenPayload(UserId userId, UserRole role) {}
}
