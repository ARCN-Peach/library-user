package com.library.user.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class User {

    private final UserId id;
    private Name name;
    private Email email;
    private String passwordHash;
    private final UserRole role;
    private UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            UserId id,
            Name name,
            Email email,
            String passwordHash,
            UserRole role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.email = Objects.requireNonNull(email, "email is required");
        this.passwordHash = requirePasswordHash(passwordHash);
        this.role = Objects.requireNonNull(role, "role is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static User registerReader(Name name, Email email, String passwordHash, Instant now) {
        return new User(UserId.newId(), name, email, passwordHash, UserRole.READER, UserStatus.ACTIVE, now, now);
    }

    public static User bootstrapLibrarian(Name name, Email email, String passwordHash, Instant now) {
        return new User(UserId.newId(), name, email, passwordHash, UserRole.LIBRARIAN, UserStatus.ACTIVE, now, now);
    }

    public static User rehydrate(
            UserId id,
            Name name,
            Email email,
            String passwordHash,
            UserRole role,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new User(id, name, email, passwordHash, role, status, createdAt, updatedAt);
    }

    public void updateProfile(Name newName, Email newEmail, Instant now) {
        if (role != UserRole.READER && !this.email.equals(newEmail)) {
            throw new IllegalStateException("librarian email cannot be changed");
        }
        this.name = Objects.requireNonNull(newName, "name is required");
        this.email = Objects.requireNonNull(newEmail, "email is required");
        this.updatedAt = Objects.requireNonNull(now, "now is required");
    }

    public boolean block(Instant now) {
        if (status == UserStatus.BLOCKED) {
            return false;
        }
        this.status = UserStatus.BLOCKED;
        this.updatedAt = Objects.requireNonNull(now, "now is required");
        return true;
    }

    public boolean unblock(Instant now) {
        if (status == UserStatus.ACTIVE) {
            return false;
        }
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Objects.requireNonNull(now, "now is required");
        return true;
    }

    public void assertCanAuthenticate() {
        if (status == UserStatus.BLOCKED) {
            throw new IllegalStateException("blocked users cannot authenticate");
        }
    }

    private static String requirePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash is required");
        }
        return passwordHash;
    }

    public UserId id() {
        return id;
    }

    public Name name() {
        return name;
    }

    public Email email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public UserRole role() {
        return role;
    }

    public UserStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
