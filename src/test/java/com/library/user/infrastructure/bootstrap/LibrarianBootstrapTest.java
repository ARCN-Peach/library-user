package com.library.user.infrastructure.bootstrap;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.library.user.application.usecase.BootstrapLibrarianUseCase;
import com.library.user.infrastructure.config.BootstrapLibrarianProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;

class LibrarianBootstrapTest {

    @Test
    void runBootstrapsWhenEnabled() throws Exception {
        var useCase = Mockito.mock(BootstrapLibrarianUseCase.class);
        var bootstrap = new LibrarianBootstrap(
                new BootstrapLibrarianProperties(true, "Admin", "admin@test.com", "Password123"),
                useCase
        );

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        verify(useCase).execute("Admin", "admin@test.com", "Password123");
    }

    @Test
    void runDoesNothingWhenDisabled() throws Exception {
        var useCase = Mockito.mock(BootstrapLibrarianUseCase.class);
        var bootstrap = new LibrarianBootstrap(
                new BootstrapLibrarianProperties(false, "Admin", "admin@test.com", "Password123"),
                useCase
        );

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        verify(useCase, never()).execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
