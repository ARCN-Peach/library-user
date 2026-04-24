package com.library.user.domain.model;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "userId is required");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }
}
