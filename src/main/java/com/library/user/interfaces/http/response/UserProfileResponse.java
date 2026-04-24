package com.library.user.interfaces.http.response;

import com.library.user.application.dto.UserProfileView;
import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String name,
        String email,
        String role,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserProfileResponse from(UserProfileView view) {
        return new UserProfileResponse(
                view.userId(),
                view.name(),
                view.email(),
                view.role(),
                view.status(),
                view.createdAt(),
                view.updatedAt()
        );
    }
}
