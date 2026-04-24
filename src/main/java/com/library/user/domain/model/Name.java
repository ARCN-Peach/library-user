package com.library.user.domain.model;

import java.util.Objects;

public record Name(String value) {

    public Name {
        Objects.requireNonNull(value, "name is required");
        var normalized = value.trim();
        if (normalized.isBlank() || normalized.length() > 120) {
            throw new IllegalArgumentException("name is invalid");
        }
        value = normalized;
    }
}
