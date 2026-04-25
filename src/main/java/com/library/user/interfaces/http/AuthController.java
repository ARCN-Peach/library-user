package com.library.user.interfaces.http;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.library.user.application.usecase.LoginUseCase;
import com.library.user.application.usecase.RefreshTokenUseCase;
import com.library.user.application.usecase.RegisterUserUseCase;
import com.library.user.interfaces.http.request.LoginRequest;
import com.library.user.interfaces.http.request.RefreshTokenRequest;
import com.library.user.interfaces.http.request.RegisterUserRequest;
import com.library.user.interfaces.http.response.AuthResponse;
import com.library.user.interfaces.http.response.UserProfileResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse register(
            @Valid @RequestBody RegisterUserRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId
    ) {
        var effectiveCorrelationId = (correlationId == null || correlationId.isBlank())
            ? (MDC.get("correlationId") != null ? MDC.get("correlationId") : UUID.randomUUID().toString())
            : correlationId;
        return UserProfileResponse.from(registerUserUseCase.execute(
                request.name(),
                request.email(),
                request.password(),
            effectiveCorrelationId
        ));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(loginUseCase.execute(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return AuthResponse.from(refreshTokenUseCase.execute(request.refreshToken()));
    }
}
