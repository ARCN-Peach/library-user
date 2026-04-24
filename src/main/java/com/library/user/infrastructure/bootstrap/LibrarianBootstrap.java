package com.library.user.infrastructure.bootstrap;

import com.library.user.application.usecase.BootstrapLibrarianUseCase;
import com.library.user.infrastructure.config.BootstrapLibrarianProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class LibrarianBootstrap implements ApplicationRunner {

    private final BootstrapLibrarianProperties properties;
    private final BootstrapLibrarianUseCase bootstrapLibrarianUseCase;

    public LibrarianBootstrap(BootstrapLibrarianProperties properties, BootstrapLibrarianUseCase bootstrapLibrarianUseCase) {
        this.properties = properties;
        this.bootstrapLibrarianUseCase = bootstrapLibrarianUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }
        bootstrapLibrarianUseCase.execute(properties.name(), properties.email(), properties.password());
    }
}
