package com.library.user.application.dto;

import com.library.user.domain.model.User;
import java.time.Instant;
import java.util.UUID;

public record UserProfileView(
        UUID userId,
        String name,
        String email,
        String role,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserProfileView from(User user) {
        return new UserProfileView(
                user.id().value(),
                user.name().value(),
                user.email().value(),
                user.role().name(),
                user.status().name(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
