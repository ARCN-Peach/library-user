package com.library.user.interfaces.http;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.user.application.usecase.ChangeUserStatusUseCase;
import com.library.user.application.usecase.GetUserProfileUseCase;
import com.library.user.application.usecase.UpdateOwnProfileUseCase;
import com.library.user.infrastructure.security.AuthenticatedUser;
import com.library.user.interfaces.http.request.ChangeUserStatusRequest;
import com.library.user.interfaces.http.request.UpdateUserProfileRequest;
import com.library.user.interfaces.http.response.UserProfileResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateOwnProfileUseCase updateOwnProfileUseCase;
    private final ChangeUserStatusUseCase changeUserStatusUseCase;

    public UserController(
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateOwnProfileUseCase updateOwnProfileUseCase,
            ChangeUserStatusUseCase changeUserStatusUseCase
    ) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateOwnProfileUseCase = updateOwnProfileUseCase;
        this.changeUserStatusUseCase = changeUserStatusUseCase;
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return UserProfileResponse.from(getUserProfileUseCase.execute(principal.userId()));
    }

    @PatchMapping("/me")
    public UserProfileResponse updateMe(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return UserProfileResponse.from(updateOwnProfileUseCase.execute(
                principal.userId(),
                request.name(),
                request.email()
        ));
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/{userId}")
    public UserProfileResponse getById(@PathVariable UUID userId) {
        return UserProfileResponse.from(getUserProfileUseCase.execute(userId));
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PatchMapping("/{userId}/status")
    public UserProfileResponse changeStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeUserStatusRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId
    ) {
        var effectiveCorrelationId = (correlationId == null || correlationId.isBlank())
            ? (MDC.get("correlationId") != null ? MDC.get("correlationId") : UUID.randomUUID().toString())
            : correlationId;
        return UserProfileResponse.from(changeUserStatusUseCase.execute(
                userId,
                request.blocked(),
                request.reason(),
            effectiveCorrelationId
        ));
    }
}
