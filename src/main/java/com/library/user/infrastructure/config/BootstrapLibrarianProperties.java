package com.library.user.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap.librarian")
public record BootstrapLibrarianProperties(
        boolean enabled,
        String name,
        String email,
        String password
) {}
