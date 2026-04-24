package com.library.user.interfaces.http.response;

import com.library.user.application.dto.AuthTokens;

public record AuthResponse(String accessToken, String refreshToken) {
    public static AuthResponse from(AuthTokens tokens) {
        return new AuthResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
