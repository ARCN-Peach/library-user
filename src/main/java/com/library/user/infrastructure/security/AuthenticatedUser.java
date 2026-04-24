package com.library.user.infrastructure.security;

import com.library.user.domain.model.UserRole;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, UserRole role) {}
