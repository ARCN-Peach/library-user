package com.library.user.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Email {
        Objects.requireNonNull(value, "email is required");
        var normalized = value.trim().toLowerCase();
        if (normalized.isBlank() || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("email is invalid");
        }
        value = normalized;
    }
}
